package ru.solom.magiclamp.alarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import ru.solom.magiclamp.databinding.FragmentAlarmsBinding

@AndroidEntryPoint
class AlarmsFragment : Fragment() {
    private var _binding: FragmentAlarmsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlarmsViewModel by viewModels()

    private var adapter: AlarmsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentAlarmsBinding.inflate(inflater, container, false).root.also {
        _binding = FragmentAlarmsBinding.bind(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AlarmsAdapter(
            onTimeClick = viewModel::onTimeClicked,
            onEnabledSwitch = viewModel::onEnabledSwitch
        )
        binding.recyclerAlarms.adapter = adapter
        binding.recyclerAlarms.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        viewLifecycleOwner.lifecycle.coroutineScope.launchWhenResumed {
            viewModel.alarmsState.collect { state ->
                state.data?.let { adapter?.update(it) }
                state.error?.let { Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show() }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        adapter = null
        super.onDestroyView()
    }
}
