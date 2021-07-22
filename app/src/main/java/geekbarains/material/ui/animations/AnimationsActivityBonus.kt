package geekbarains.material.ui.animations

import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import geekbarains.material.R
import kotlinx.android.synthetic.main.activity_animations_bonus_start.*

class AnimationsActivityBonus : AppCompatActivity() {

    private var show = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animations_bonus_start)

        backgroundImage.setOnClickListener { if (show) hideComponents() else showComponents() }
    }

    private fun showComponents() {
        show = true
        applyAnimation(R.layout.activity_animations_bonus_end)
    }

    private fun hideComponents() {
        show = false
        applyAnimation(R.layout.activity_animations_bonus_start)
    }

    private fun applyAnimation(@LayoutRes layout : Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this, layout)

        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(1.0f)
        transition.duration = 1200

        TransitionManager.beginDelayedTransition(constraint_container, transition)
        constraintSet.applyTo(constraint_container)
    }
}