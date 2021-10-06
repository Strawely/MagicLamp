package ru.solom.magiclamp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import ru.solom.magiclamp.databinding.FragmentMainBinding
import kotlin.math.roundToInt


@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMainBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)

        with(binding!!) {
            btnPower.setOnClickListener {
                viewModel.onPowerBtnClick()
            }
            sliderBrightness.addOnChangeListener { _, value, fromUser ->
                if (fromUser) viewModel.onBrightnessChanged(value.roundToInt())
            }
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launchWhenStarted {
            viewModel.mainState.collect { state ->
                val btnRes = if (state.lampState.isOn) {
                    R.drawable.ic_power_24
                } else {
                    R.drawable.ic_power_outlined_24
                }
                binding!!.btnPower.setImageResourceWithAnim(btnRes)
                binding!!.textLampAddress.text = state.address ?: "Not connected"
                binding!!.sliderBrightness.value = state.lampState.brightness.toFloat()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}
