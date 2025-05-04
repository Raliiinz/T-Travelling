package ru.itis.travelling.presentation.addtrip.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.travelling.R
import ru.itis.travelling.databinding.DialogAddTripBinding
import kotlin.getValue
import kotlin.math.log

@AndroidEntryPoint
class AddTripBottomSheet : BottomSheetDialogFragment(R.layout.dialog_add_trip) {

    private val viewBinding: DialogAddTripBinding by viewBinding(DialogAddTripBinding::bind)
    private val viewModel: AddTripViewModel by viewModels()

    private val phoneNumber: String by lazy {
        arguments?.getString(PHONE_NUMBER) ?: ""
    }

    private var navigationPending = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Здесь можно инициализировать RecyclerView и другие элементы
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnCancelListener {
            if (!navigationPending) {
                navigationPending = true
                performNavigationAndDismiss()
            }
        }
//        dialog.setOnCancelListener {
//            // При отмене (клик вне или кнопка назад) переходим на TripsFragment
//            navigateToTripsFragment()
//        }

//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnShowListener {
            // 4. Обработка клика на затемненную область
            dialog.window?.decorView?.setOnClickListener { view ->
                if (!navigationPending) {
                    navigationPending = true
                    performNavigationAndDismiss()
                }
            }

            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet).apply {
                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_HIDDEN && !navigationPending) {
                                // 3. Обработка закрытия свайпом вниз
                                navigationPending = true
                                performNavigationAndDismiss()
                            }
                        }
                        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                    })

                    val maxHeight = (resources.displayMetrics.heightPixels * 0.75).toInt()
                    sheet.layoutParams.height = maxHeight
                    peekHeight = maxHeight
                    state = BottomSheetBehavior.STATE_EXPANDED
                    sheet.setBackgroundColor(Color.WHITE)
                }
            }
        }
//        dialog.setOnShowListener {
//            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.let { sheet ->
//                val behavior = BottomSheetBehavior.from(sheet)
//
//
//                // Устанавливаем высоту 3/4 экрана
//                val displayMetrics = resources.displayMetrics
//                val maxHeight = (displayMetrics.heightPixels * 0.75).toInt()
//
//                sheet.layoutParams.height = maxHeight
//                behavior.peekHeight = maxHeight
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
//
//                sheet.setBackgroundColor(Color.WHITE)
//
//                // Обработчик состояния BottomSheet
//                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//                    override fun onStateChanged(bottomSheet: View, newState: Int) {
//                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
//
//                            navigateToTripsFragment()
//                        }
//                    }
//
//                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
//                })
//            }
//        }

        return dialog
    }

    private fun performNavigationAndDismiss() {
        // 4. Сначала навигация, потом dismiss
        Log.d("AddTripBottomSheet", "Navigating to trips, phone: ${phoneNumber}")
        viewModel.navigateToTrips(phoneNumber)
        dismissAllowingStateLoss()
    }
//    private fun navigateToTripsFragment() {
//        viewModel.navigateToTrips(phoneNumber)
//
//        dismiss()
//    }

    companion object {
        const val TAG = "AddTripBottomSheet"
        private const val PHONE_NUMBER = "phone_number"

        fun newInstance(phoneNumber: String): AddTripBottomSheet {
            return AddTripBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(PHONE_NUMBER, phoneNumber)
                }
            }
        }
    }
}