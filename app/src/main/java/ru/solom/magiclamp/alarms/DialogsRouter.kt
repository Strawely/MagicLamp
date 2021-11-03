package ru.solom.magiclamp.alarms

import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import ru.solom.magiclamp.ActivityProvider
import javax.inject.Inject

class DialogsRouter @Inject constructor(private val activityProvider: ActivityProvider) {
    private val fragmentManager get() = activityProvider.requireActivity().supportFragmentManager
    private var timePicker: MaterialTimePicker? = null

    fun showTimePicker(time: Time, onSuccess: (Time) -> Unit) {
        timePicker?.dismiss()
        timePicker = MaterialTimePicker.Builder()
            .setHour(time.hours)
            .setMinute(time.minutes)
            .setTimeFormat(CLOCK_24H)
            .build().also { picker ->
                picker.addOnPositiveButtonClickListener { _ ->
                    onSuccess(Time(picker.hour, picker.minute))
                }
                picker.show(fragmentManager, TIME_PICKER_TAG)
            }
    }
}

private const val TIME_PICKER_TAG = "time_picker"
