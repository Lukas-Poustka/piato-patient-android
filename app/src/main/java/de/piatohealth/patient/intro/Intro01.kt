package de.piatohealth.patient.intro

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import de.piatohealth.patient.R

class Intro01 : Fragment() {
    companion object {
        val TAG: String = Intro01::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.intro_01, container, false)

        var buttonEnabled = false

        val intro01Condition = rootView.findViewById<AppCompatTextView>(R.id.intro_01_condition)
        intro01Condition.text =
            Html.fromHtml(getString(R.string.intro_01_condition), Html.FROM_HTML_MODE_COMPACT)

        val buttonUnselected = rootView.findViewById<AppCompatTextView>(R.id.button_condition_unselected)
        val buttonSelected = rootView.findViewById<AppCompatTextView>(R.id.button_condition_selected)
        val intro01SetUpNow = rootView.findViewById<AppCompatTextView>(R.id.intro_01_set_up_now)

        buttonUnselected.setOnClickListener {
            buttonUnselected.visibility = View.INVISIBLE
            buttonSelected.visibility = View.VISIBLE
            intro01SetUpNow.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_green100_corners_round_32dp)
            buttonEnabled = true
        }

        buttonSelected.setOnClickListener {
            buttonSelected.visibility = View.INVISIBLE
            buttonUnselected.visibility = View.VISIBLE
            intro01SetUpNow.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_gray50_corners_round_32dp)
            buttonEnabled = false
        }


        intro01SetUpNow.setOnClickListener {
            if (buttonEnabled) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, Intro02(), Intro02.TAG)
                    .addToBackStack(Intro02.TAG)
                    .commitAllowingStateLoss()
            }
        }

        return rootView
    }
}