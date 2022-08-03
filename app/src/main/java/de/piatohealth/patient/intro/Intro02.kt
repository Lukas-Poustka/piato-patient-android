package de.piatohealth.patient.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import de.piatohealth.patient.R

class Intro02 : Fragment() {
    companion object {
        val TAG: String = Intro02::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.intro_02, container, false)

        val intro02GenerateQRCode = rootView.findViewById<AppCompatTextView>(R.id.intro_02_generate_qr_code)
        intro02GenerateQRCode.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, Intro02(), Intro02.TAG)
                .addToBackStack(Intro02.TAG)
                .commitAllowingStateLoss()
        }

        return rootView
    }
}