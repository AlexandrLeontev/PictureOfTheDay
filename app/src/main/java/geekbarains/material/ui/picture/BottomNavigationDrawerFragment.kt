package geekbarains.material.ui.picture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import geekbarains.material.R
import kotlinx.android.synthetic.main.bottom_navigation_layout.*
//Класс диалогового окна
class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {
    //BottomSheetDialogFragment() содержит всю логику, затемнение, анимацию на главном фрагменте

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_navigation_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        navigation_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_one -> {
                    Toast.makeText(context, "1", Toast.LENGTH_SHORT).show()
                    dismiss()
                    // dismiss() нужен чтобы вьюшка закрывалась после выбора
                }
                R.id.navigation_two -> {
                    Toast.makeText(context, "2", Toast.LENGTH_SHORT).show()
                dismiss()
                }
            }
            true
        }
    }
}
