package ru.solom.magiclamp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import ru.solom.magiclamp.R
import ru.solom.magiclamp.databinding.FragmentMainBinding
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentMainBinding? = null
    private var effectsAdapter: EffectsAdapter? = null

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

        effectsAdapter = EffectsAdapter(viewModel::onItemClick)

        with(binding!!) {
            btnPower.setOnClickListener {
                viewModel.onPowerBtnClick()
            }
            brightnessSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) viewModel.onBrightnessChanged(value.roundToInt())
            }
            speedSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) viewModel.onSpeedChanged(value.roundToInt())
            }
            scaleSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) viewModel.onScaleChanged(value.roundToInt())
            }
            effectsList.layoutManager = LinearLayoutManager(requireContext())
            effectsList.adapter = effectsAdapter
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launchWhenStarted {
            viewModel.mainState.collect { state ->
                val btnRes = if (state.lampState.isOn) {
                    R.drawable.ic_power_24
                } else {
                    R.drawable.ic_power_outlined_24
                }

                with(binding!!) {
                    setSlidersConstraints()
                    btnPower.setImageResourceWithAnim(btnRes)
                    textLampAddress.text = state.address ?: "Not connected"
                    brightnessSlider.value = state.lampState.brightness.toFloat()
                    speedSlider.value = state.lampState.speed.toFloat()
                    scaleSlider.value = state.lampState.scale.toFloat()
                    effectsAdapter?.updateCurrent(state.lampState.currentId)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.effects.collect { effects ->
                effectsAdapter!!.updateEffects(effects)
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        effectsAdapter = null
        super.onDestroyView()
    }

    private fun setSlidersConstraints() {
        val currentEffect = viewModel.effects.value.firstOrNull {
            it.id == viewModel.mainState.value.lampState.currentId
        } ?: return
        if (currentEffect.scaleUnavailable) {
            binding!!.scaleSlider.isEnabled = false
        } else {
            binding!!.scaleSlider.isEnabled = true
            binding!!.scaleSlider.valueFrom = currentEffect.minScale.toFloat()
            binding!!.scaleSlider.valueTo = currentEffect.maxScale.toFloat()
        }
        if (currentEffect.speedUnavailable) {
            binding!!.speedSlider.isEnabled = false
        } else {
            binding!!.speedSlider.isEnabled = true
            binding!!.speedSlider.valueFrom = currentEffect.minSpeed.toFloat()
            binding!!.speedSlider.valueTo = currentEffect.maxSpeed.toFloat()
        }
    }
}
