import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.learning.androidlearning.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RedPacketTipDialog : DialogFragment() {
    private var content: String? = null
    private var dismissJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.RedPacketTipDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_red_packet_tip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvContent).text = content
        
        // 启动自动消失倒计时
        startDismissTimer()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            attributes = attributes.apply {
                y = context.resources.getDimensionPixelSize(R.dimen.dialog_margin_top)
                dimAmount = 0f
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }
            setWindowAnimations(R.style.RedPacketTipAnimation)
        }
    }

    private fun startDismissTimer() {
        dismissJob?.cancel()
        dismissJob = lifecycleScope.launch {
            delay(3000) // 3秒后自动消失
            dismiss()
        }
    }

    override fun onDestroyView() {
        dismissJob?.cancel()
        dismissJob = null
        super.onDestroyView()
    }

    companion object {
        fun show(fragmentManager: FragmentManager, content: String) {
            RedPacketTipDialog().apply {
                this.content = content
            }.show(fragmentManager, "RedPacketTipDialog")
        }
    }
} 