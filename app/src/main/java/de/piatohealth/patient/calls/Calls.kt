package de.piatohealth.patient.calls


/*import android.content.*
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import de.piatohealth.patient.R
import de.piatohealth.patient.db.*
import de.piatohealth.patient.helpers.Const
import de.piatohealth.patient.helpers.FunctionHelper
import de.piatohealth.patient.model.*
import de.piatohealth.patient.patients.Patients
import de.piatohealth.patient.profile.Profile
import de.piatohealth.patient.sync.JobManager
import de.piatohealth.patient.sync.SyncJob
import org.json.JSONArray
import java.util.*


class Calls : Fragment() {
    companion object {
        val TAG: String = Calls::class.java.simpleName
        lateinit var sp: SharedPreferences
        lateinit var patientsData: ArrayList<PatientData>
        lateinit var allActivatedPatients: ArrayList<ActivatedPatient>
        lateinit var personalCallAdapter: PersonalCallRecyclerViewAdapter
        lateinit var personalCallRecyclerView: RecyclerView
        lateinit var otherCallAdapter: OtherCallRecyclerViewAdapter
        lateinit var otherCallRecyclerView: RecyclerView
        lateinit var rooms: ArrayList<Room>
        lateinit var nurses: ArrayList<Nurse>
        lateinit var personalCalls: ArrayList<PersonalCall>
        lateinit var otherCalls: ArrayList<OtherCall>
        lateinit var personalCallNo: TextView
        lateinit var otherCallNo: TextView
        lateinit var deletedPersonalCall: PersonalCall
        lateinit var deletedOtherCall: OtherCall
        private var swipeIcon: Drawable? = null
        private var swipeBackground: ColorDrawable? = null
        const val PAIN: String = "pain"
        const val TOILET: String = "toilet"
        const val THIRST: String = "thirst"
        const val INFUSION: String = "infusion"

        const val TEA: String = "tea"

        const val ACTIVE: String = "active"
        const val ANSWERED: String = "answered"
        const val IN_PROGRESS: String = "inProgress"

        const val SWIPE_ALLOWED: String = "swipe-allowed"
        const val SWIPE_DISALLOWED: String = "swipe-disallowed"
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(refreshCallsReceiver, IntentFilter(Const.REFRESH_CALLS))
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(scheduleCallsUpdateReceiver, IntentFilter(Const.SCHEDULE_CALLS_UPDATE))
        RefreshHelper.scheduleNextRefresh(requireContext())
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireActivity())
            .unregisterReceiver(refreshCallsReceiver)
        LocalBroadcastManager.getInstance(requireActivity())
            .unregisterReceiver(scheduleCallsUpdateReceiver)
        FunctionHelper.setOpenRequestsZero(requireContext())
        RefreshHelper.cancelRefresh(requireContext())
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.calls, container, false)

        sp = requireContext().getSharedPreferences(Const.GLOBAL, Context.MODE_PRIVATE)
        swipeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_unsubscribe_call)
        swipeBackground = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.gray_100))


        val profileIcon = rootView.findViewById<AppCompatImageView>(R.id.icon_profile)
        profileIcon.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, Profile(), Profile.TAG)
                .addToBackStack(Profile.TAG)
                .commitAllowingStateLoss()
        }

        allActivatedPatients = MySQLiteActivatedPatientHelper(requireContext()).allActivatedPatients
        patientsData = MySQLitePatientDataHelper(requireContext()).allPatientData
        rooms = MySQLiteRoomHelper(requireContext()).allRooms
        nurses = MySQLiteNurseHelper(requireContext()).allNurses
        personalCalls = MySQLitePersonalCallHelper(requireContext()).allPersonalCalls
        otherCalls = MySQLiteOtherCallHelper(requireContext()).allOtherCalls

        personalCallAdapter = PersonalCallRecyclerViewAdapter(personalCalls)
        personalCallRecyclerView = rootView.findViewById(R.id.personal_call_list)
        personalCallRecyclerView.adapter = personalCallAdapter
        val itemTouchHelperPersonalCall = ItemTouchHelper(simpleCallbackPersonalCall)
        itemTouchHelperPersonalCall.attachToRecyclerView(personalCallRecyclerView)

        otherCallAdapter = OtherCallRecyclerViewAdapter(otherCalls)
        otherCallRecyclerView = rootView.findViewById(R.id.other_call_list)
        otherCallRecyclerView.adapter = otherCallAdapter
        val itemTouchHelperOtherCall = ItemTouchHelper(simpleCallbackOtherCall)
        itemTouchHelperOtherCall.attachToRecyclerView(otherCallRecyclerView)

        personalCallNo = rootView.findViewById(R.id.personal_call_no)
        personalCallNo.text = personalCalls.size.toString()
        otherCallNo = rootView.findViewById(R.id.other_call_no)
        otherCallNo.text = otherCalls.size.toString()

        val navPatients = rootView.findViewById<TextView>(R.id.nav_patients)
        navPatients.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, Patients(), Patients.TAG)
                .addToBackStack(Patients.TAG)
                .commitAllowingStateLoss()
        }

        return rootView
    }

    private var refreshCallsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            personalCallAdapter.update(context)
            otherCallAdapter.update(context)
        }
    }

    private var scheduleCallsUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val calls = requireActivity().supportFragmentManager.findFragmentByTag(TAG)
            if (calls != null && calls.isVisible) {
                RefreshHelper.scheduleNextRefresh(context)
            }
        }
    }

    private val simpleCallbackPersonalCall =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val startPosition = viewHolder.adapterPosition
                val endPosition = target.adapterPosition

                Collections.swap(personalCalls, startPosition, endPosition)
                recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
                return true
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (viewHolder.itemView.tag == SWIPE_ALLOWED) {
                    super.getSwipeDirs(recyclerView, viewHolder)
                } else {
                    0
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView

                /*val layout = LinearLayout(context)

                val textView = TextView(context)
                textView.visibility = View.VISIBLE
                textView.text = "Hello world"
                layout.addView(textView)

                layout.measure(c.width, c.height)
                layout.layout(0, 0, c.width, c.height)

                // To place the text view somewhere specific:
                //canvas.translate(0, 0);


                // To place the text view somewhere specific:
                //canvas.translate(0, 0);
                layout.draw(c)*/


                //val text = Html.fromHtml(getString(R.string.move_to_other_calls), Html.FROM_HTML_MODE_COMPACT)

                val textPaint = TextPaint()
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 16 * resources.displayMetrics.density
                textPaint.setARGB(80, 0, 0, 0)

                //val xPos = c.width / 2
                //val yPos = c.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2 //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
                //c.drawText(text.toString(), xPos.toFloat(), yPos, textPaint)

                /*val iconMargin: Int = (itemView.height - swipeIcon!!.intrinsicHeight) / 2
                val iconTop: Int = itemView.top + (itemView.height - swipeIcon!!.intrinsicHeight) / 2
                val iconBottom: Int = iconTop + swipeIcon!!.intrinsicHeight*/

                if (dX > 0) {
                    /* Set your color for positive displacement */
                    // Draw Rect with varying right side, equal to displacement dX
                    /*val iconLeft: Int = itemView.left + iconMargin + swipeIcon!!.intrinsicWidth
                    val iconRight = itemView.left + iconMargin
                    swipeIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    swipeBackground?.setBounds(itemView.left,
                        itemView.top,
                        dX.toInt(),
                        itemView.bottom
                    )*/
                    c.drawRect(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        dX,
                        itemView.bottom.toFloat(),
                        textPaint
                    )
                } else if (dX < 0) {
                    /* Set your color for negative displacement */
                    // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                    /*val iconLeft: Int = itemView.right - iconMargin - swipeIcon!!.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    swipeIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)


                    swipeBackground?.setBounds(itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )*/
                    c.drawRect(
                        itemView.right.toFloat() + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        textPaint
                    )
                } else {
                    //swipeBackground?.setBounds(0, 0, 0, 0);
                }
                //swipeBackground?.draw(c)
                //swipeIcon?.draw(c)


                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val position = holder.adapterPosition
                when (direction) {
                    ItemTouchHelper.RIGHT -> {

                        val nurseKeys = JSONArray()
                        val jsonArray = JSONArray(personalCalls[position].nurseKeys)
                        for (i in 0 until jsonArray.length()) {
                            if (jsonArray[i] != sp.getString(Const.OWN_NURSE_KEY, "")) {
                                nurseKeys.put(jsonArray[i])
                            }
                        }

                        val deletedPersonalCall = personalCalls[position]
                        MySQLitePersonalCallHelper(holder.itemView.context).deletePersonalCall(
                            deletedPersonalCall.key!!
                        )
                        personalCallAdapter.update(holder.itemView.context)
                        MySQLiteOtherCallHelper(holder.itemView.context).addOtherCall(
                            deletedPersonalCall.key!!,
                            deletedPersonalCall.stateKey!!,
                            deletedPersonalCall.patientKey!!,
                            deletedPersonalCall.type!!,
                            deletedPersonalCall.detail!!,
                            deletedPersonalCall.isEscalated!!,
                            deletedPersonalCall.state!!,
                            deletedPersonalCall.minutes!!,
                            nurseKeys.toString(),
                            deletedPersonalCall.isRed!!
                        )
                        otherCallAdapter.update(holder.itemView.context)

                        Snackbar
                            .make(
                                personalCallRecyclerView,
                                Html.fromHtml(
                                    getString(R.string.call_move_to_other_calls),
                                    Html.FROM_HTML_MODE_COMPACT
                                ),
                                Snackbar.LENGTH_LONG
                            )
                            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onShown(transientBottomBar: Snackbar?) {
                                    super.onShown(transientBottomBar)
                                }

                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                        JobManager(holder.itemView.context).startOneTimeSyncJobCallKey(
                                            SyncJob.UNSUBSCRIBE_CALL,
                                            JobManager.JOB_ID_UNSUBSCRIBE_CALL,
                                            deletedPersonalCall.key!!
                                        )
                                    }
                                    super.onDismissed(transientBottomBar, event)
                                }
                            })
                            .setAction(R.string.backwards) {
                                MySQLiteOtherCallHelper(holder.itemView.context).deleteOtherCall(
                                    deletedPersonalCall.key!!
                                )
                                otherCallAdapter.update(holder.itemView.context)
                                MySQLitePersonalCallHelper(holder.itemView.context).addPersonalCall(
                                    deletedPersonalCall.key!!,
                                    deletedPersonalCall.stateKey!!,
                                    deletedPersonalCall.patientKey!!,
                                    deletedPersonalCall.type!!,
                                    deletedPersonalCall.detail!!,
                                    deletedPersonalCall.isEscalated!!,
                                    deletedPersonalCall.state!!,
                                    deletedPersonalCall.minutes!!,
                                    deletedPersonalCall.nurseKeys!!,
                                    deletedPersonalCall.isRed!!
                                )
                                personalCallAdapter.update(holder.itemView.context)
                            }
                            .show()
                    }
                }
            }
        }

    private val simpleCallbackOtherCall =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val startPosition = viewHolder.adapterPosition
                val endPosition = target.adapterPosition

                Collections.swap(otherCalls, startPosition, endPosition)
                recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
                return true
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView

                val textPaint = TextPaint()
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 16 * resources.displayMetrics.density
                textPaint.setARGB(80, 0, 0, 0)

                if (dX > 0) {
                    /* Set your color for positive displacement */
                    // Draw Rect with varying right side, equal to displacement dX
                    c.drawRect(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        dX,
                        itemView.bottom.toFloat(),
                        textPaint
                    )
                } else if (dX < 0) {
                    /* Set your color for negative displacement */
                    // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                    c.drawRect(
                        itemView.right.toFloat() + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        textPaint
                    )
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (viewHolder.itemView.tag == SWIPE_ALLOWED) {
                    super.getSwipeDirs(recyclerView, viewHolder)
                } else {
                    0
                }
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val position = holder.adapterPosition
                when (direction) {
                    ItemTouchHelper.RIGHT -> {

                        deletedOtherCall = otherCalls[position]
                        MySQLiteOtherCallHelper(holder.itemView.context).deleteOtherCall(
                            deletedOtherCall.key!!
                        )
                        otherCallAdapter.update(holder.itemView.context)
                        MySQLitePersonalCallHelper(holder.itemView.context).addPersonalCall(
                            deletedOtherCall.key!!,
                            deletedOtherCall.stateKey!!,
                            deletedOtherCall.patientKey!!,
                            deletedOtherCall.type!!,
                            deletedOtherCall.detail!!,
                            deletedOtherCall.isEscalated!!,
                            ANSWERED,
                            deletedOtherCall.minutes!!,
                            "[" + sp.getString(Const.OWN_NURSE_KEY, "") + "]",
                            deletedOtherCall.isRed!!
                        )
                        personalCallAdapter.update(holder.itemView.context)

                        Snackbar
                            .make(
                                otherCallRecyclerView,
                                Html.fromHtml(
                                    getString(R.string.call_move_to_my_calls),
                                    Html.FROM_HTML_MODE_COMPACT
                                ),
                                Snackbar.LENGTH_LONG
                            )
                            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onShown(transientBottomBar: Snackbar?) {
                                    super.onShown(transientBottomBar)
                                }

                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                        JobManager(holder.itemView.context).startOneTimeSyncJobCallKey(
                                            SyncJob.SUBSCRIBE_CALL,
                                            JobManager.JOB_ID_SUBSCRIBE_CALL,
                                            deletedOtherCall.key!!
                                        )
                                    }
                                    super.onDismissed(transientBottomBar, event)
                                }
                            })
                            .setAction(R.string.backwards) {
                                MySQLitePersonalCallHelper(holder.itemView.context).deletePersonalCall(
                                    deletedOtherCall.key!!
                                )
                                personalCallAdapter.update(holder.itemView.context)
                                MySQLiteOtherCallHelper(holder.itemView.context).addOtherCall(
                                    deletedOtherCall.key!!,
                                    deletedOtherCall.stateKey!!,
                                    deletedOtherCall.patientKey!!,
                                    deletedOtherCall.type!!,
                                    deletedOtherCall.detail!!,
                                    deletedOtherCall.isEscalated!!,
                                    deletedOtherCall.state!!,
                                    deletedOtherCall.minutes!!,
                                    deletedOtherCall.nurseKeys!!,
                                    deletedOtherCall.isRed!!
                                )
                                otherCallAdapter.update(holder.itemView.context)
                            }
                            .show()
                    }
                }
            }
        }

    class PersonalCallRecyclerViewAdapter(items: ArrayList<PersonalCall>) :
        RecyclerView.Adapter<PersonalCallRecyclerViewAdapter.ViewHolder>() {
        private val mValues: ArrayList<PersonalCall>
        lateinit var context: Context

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.call_card, parent, false)
            return ViewHolder(view)
        }

        fun update(context: Context) {
            personalCalls.clear()
            personalCalls.addAll(MySQLitePersonalCallHelper(context).allPersonalCalls)
            this.notifyDataSetChanged()
            personalCallNo.text = personalCalls.size.toString()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mEye.visibility = View.INVISIBLE
            holder.mProgress.visibility = View.INVISIBLE
            holder.mItem.background = ContextCompat.getDrawable(
                holder.mView.context,
                R.drawable.card_white_corners_round_8dp
            )
            holder.mCallNav.visibility = View.GONE

            if ((mValues[position].state == ACTIVE || mValues[position].state == ANSWERED) && mValues[position].isRed == false) {
                holder.mView.tag = SWIPE_ALLOWED
            } else {
                holder.mView.tag = SWIPE_DISALLOWED
            }
            holder.mPatientName.text =
                patientsData.firstOrNull { it.key == mValues[position].patientKey }?.firstName + " " + patientsData.firstOrNull { it.key == mValues[position].patientKey }?.lastName
            val roomKey = allActivatedPatients.firstOrNull { it.key == mValues[position].patientKey }?.roomKey
            holder.mRoom.text = rooms.firstOrNull { it.key == roomKey }?.title
            holder.mCounter.text =
                if (mValues[position].minutes!! > 99) "99" else mValues[position].minutes.toString()
            val jArray = JSONArray(mValues[position].nurseKeys)
            if (jArray.length() > 1) {
                holder.mNurseName.text = jArray.length().toString()
                holder.mNurseName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_patients,
                    0,
                    0,
                    0
                )
            }
            when (mValues[position].type) {
                TOILET -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].isEscalated == true) R.drawable.ic_toilet_escalated else R.drawable.ic_toilet
                    )
                )
                PAIN -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].isEscalated == true) R.drawable.ic_pain_escalated else R.drawable.ic_pain
                    )
                )
                THIRST -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].detail == TEA) R.drawable.ic_tea else R.drawable.ic_water
                    )
                )
                INFUSION -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].isEscalated == true) R.drawable.ic_infusion_escalated else R.drawable.ic_infusion
                    )
                )
                else -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        R.drawable.ic_other
                    )
                )
            }
            when (mValues[position].state) {
                ANSWERED -> {
                    holder.mEye.visibility = View.VISIBLE
                }
                IN_PROGRESS -> {
                    holder.mProgress.visibility = View.VISIBLE
                    holder.mItem.background = ContextCompat.getDrawable(
                        holder.mView.context,
                        R.drawable.card_green25_corners_round_8dp
                    )
                }
            }

            if (mValues[position].isRed == true) {
                holder.mItem.background = ContextCompat.getDrawable(
                    holder.mView.context,
                    R.drawable.card_red25_corners_round_8dp
                )
            }

            holder.mBack.setOnClickListener {
                holder.mCallNav.visibility = View.GONE
            }
            holder.mComplete.setOnClickListener {
                deletedPersonalCall = mValues[position]
                MySQLitePersonalCallHelper(holder.mView.context).deletePersonalCall(
                    deletedPersonalCall.key!!
                )
                update(holder.mView.context)

                Snackbar
                    .make(
                        personalCallRecyclerView,
                        R.string.care_call_finished,
                        Snackbar.LENGTH_LONG
                    )
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onShown(transientBottomBar: Snackbar?) {
                            super.onShown(transientBottomBar)
                        }

                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                JobManager(holder.mView.context).startOneTimeSyncJobCallKey(
                                    SyncJob.COMPLETE_CALL,
                                    JobManager.JOB_ID_COMPLETE_CALL,
                                    deletedPersonalCall.key!!
                                )
                            }
                            update(holder.mView.context)
                            super.onDismissed(transientBottomBar, event)
                        }
                    })
                    .setAction(R.string.backwards) {
                        MySQLitePersonalCallHelper(holder.mView.context).addPersonalCall(
                            deletedPersonalCall.key!!,
                            deletedPersonalCall.stateKey!!,
                            deletedPersonalCall.patientKey!!,
                            deletedPersonalCall.type!!,
                            deletedPersonalCall.detail!!,
                            deletedPersonalCall.isEscalated!!,
                            deletedPersonalCall.state!!,
                            deletedPersonalCall.minutes!!,
                            deletedPersonalCall.nurseKeys!!,
                            deletedPersonalCall.isRed!!
                        )
                        update(holder.mView.context)
                    }
                    .show()
            }

            holder.mItem.setOnClickListener {
                when (mValues[position].state) {
                    ACTIVE -> {
                        JobManager(holder.mView.context).startOneTimeSyncJobCallKey(
                            SyncJob.ANSWER_CALL,
                            JobManager.JOB_ID_ANSWER_CALL,
                            mValues[position].key!!
                        )
                        MySQLitePersonalCallHelper(holder.mView.context).updatePersonalCallState(
                            mValues[position].key!!,
                            ANSWERED
                        )
                        MySQLitePersonalCallHelper(holder.mView.context).setBackgroundWhite(mValues[position].key!!)
                        update(holder.mView.context)
                    }
                    ANSWERED -> {
                        JobManager(holder.mView.context).startOneTimeSyncJobCallKey(
                            SyncJob.PROCEED_CALL,
                            JobManager.JOB_ID_PROCEED_CALL,
                            mValues[position].key!!
                        )
                        MySQLitePersonalCallHelper(holder.mView.context).updatePersonalCallState(
                            mValues[position].key!!,
                            IN_PROGRESS
                        )
                        update(holder.mView.context)
                    }
                    IN_PROGRESS -> {
                        holder.mCallNav.visibility = View.VISIBLE
                        FunctionHelper.addOpenRequest(context)
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                FunctionHelper.substractOpenRequest(context)
                            }, 5000
                        )
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        class ViewHolder(val mView: View) : RecyclerView.ViewHolder(
            mView
        ) {

            val mRoom: TextView = mView.findViewById(R.id.room)
            val mPatientName: TextView = mView.findViewById(R.id.patient_name)
            val mCallType: ImageView = mView.findViewById(R.id.call_type)
            val mCounter: TextView = mView.findViewById(R.id.counter)
            val mNurseName: TextView = mView.findViewById(R.id.nurse_name)
            val mEye: ImageView = mView.findViewById(R.id.eye)
            val mProgress: ImageView = mView.findViewById(R.id.progress)
            val mBack: TextView = mView.findViewById(R.id.back)
            val mComplete: TextView = mView.findViewById(R.id.complete)
            val mItem: ConstraintLayout = mView.findViewById(R.id.item)
            val mCallNav: ConstraintLayout = mView.findViewById(R.id.call_nav)
        }

        init {
            mValues = items
        }
    }

    class OtherCallRecyclerViewAdapter(items: ArrayList<OtherCall>) :
        RecyclerView.Adapter<OtherCallRecyclerViewAdapter.ViewHolder>() {
        private val mValues: ArrayList<OtherCall>

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.call_card, parent, false)
            return ViewHolder(view)
        }

        fun update(context: Context) {
            otherCalls.clear()
            otherCalls.addAll(MySQLiteOtherCallHelper(context).allOtherCalls)
            this.notifyDataSetChanged()
            otherCallNo.text = otherCalls.size.toString()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mEye.visibility = View.INVISIBLE
            holder.mProgress.visibility = View.INVISIBLE
            holder.mItem.background = ContextCompat.getDrawable(
                holder.mView.context,
                R.drawable.card_white_corners_round_8dp
            )
            holder.mPatientName.text =
                patientsData.firstOrNull { it.key == mValues[position].patientKey }?.firstName + " " + patientsData.firstOrNull { it.key == mValues[position].patientKey }?.lastName
            val roomKey = allActivatedPatients.firstOrNull { it.key == mValues[position].patientKey }?.roomKey
            holder.mRoom.text = rooms.firstOrNull { it.key == roomKey }?.title
            holder.mCounter.text =
                if (mValues[position].minutes!! > 99) "99" else mValues[position].minutes.toString()
            val jArray = JSONArray(mValues[position].nurseKeys)
            if (jArray.length() > 1) {
                holder.mNurseName.text = jArray.length().toString()
                holder.mNurseName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_patients,
                    0,
                    0,
                    0
                )
            } else if (jArray.length() == 1) {
                holder.mNurseName.text = nurses.firstOrNull { it.key == jArray.get(0) }?.firstName
            }
            when (mValues[position].type) {
                TOILET -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].isEscalated == true) R.drawable.ic_toilet_escalated else R.drawable.ic_toilet
                    )
                )
                PAIN -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].isEscalated == true) R.drawable.ic_pain_escalated else R.drawable.ic_pain
                    )
                )
                THIRST -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].detail == TEA) R.drawable.ic_tea else R.drawable.ic_water
                    )
                )
                INFUSION -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        if (mValues[position].isEscalated == true) R.drawable.ic_infusion_escalated else R.drawable.ic_infusion
                    )
                )
                else -> holder.mCallType.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.mView.context,
                        R.drawable.ic_other
                    )
                )
            }
            when (mValues[position].state) {
                ACTIVE -> {
                    holder.mView.tag = SWIPE_ALLOWED
                }
                ANSWERED -> {
                    holder.mEye.visibility = View.VISIBLE
                    holder.mView.tag = SWIPE_DISALLOWED
                }
                IN_PROGRESS -> {
                    holder.mProgress.visibility = View.VISIBLE
                    holder.mItem.background = ContextCompat.getDrawable(
                        holder.mView.context,
                        R.drawable.card_green25_corners_round_8dp
                    )
                    holder.mView.tag = SWIPE_DISALLOWED
                }
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
            val mRoom: TextView = mView.findViewById(R.id.room)
            val mPatientName: TextView = mView.findViewById(R.id.patient_name)
            val mCallType: ImageView = mView.findViewById(R.id.call_type)
            val mCounter: TextView = mView.findViewById(R.id.counter)
            val mNurseName: TextView = mView.findViewById(R.id.nurse_name)
            val mEye: ImageView = mView.findViewById(R.id.eye)
            val mProgress: ImageView = mView.findViewById(R.id.progress)
            val mItem: ConstraintLayout = mView.findViewById(R.id.item)
        }

        init {
            mValues = items
        }
    }
}*/