package ru.solom.magiclamp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.animation.doOnEnd

class PowerImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {
    private var prevImageResource: Int = 0

    fun setImageResourceWithAnim(@DrawableRes resId: Int) {
        if (resId == prevImageResource) return
        val set = AnimatorSet()
        set.playSequentially(
            ObjectAnimator.ofInt(this, ALPHA_PROP, TARGET_ALPHA).apply {
                doOnEnd {
                    prevImageResource = resId
                    setImageResource(resId)
                }
            },
            ObjectAnimator.ofInt(this, ALPHA_PROP, FULL_ALPHA)
        )
        set.duration = ANIM_DURATION
        set.start()
    }
}

private const val ANIM_DURATION = 300L
private const val TARGET_ALPHA = 127
private const val FULL_ALPHA = 255
private const val ALPHA_PROP = "imageAlpha"
