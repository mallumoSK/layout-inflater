package tk.mallumo.layout.inflater

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class SimpleVM : ViewModel() {
    val flowText = MutableStateFlow("initValue")
}

class SimpleFragment : Fragment() {

    val viewModel by viewModels<SimpleVM>()

    val layout by lazyLayout<LayoutSimpleLayout> {
        veverka.text = "Replaced text"
        textTemplate0.textView.text = "Included layout"
        flowUI(viewModel.flowText) { textTemplate1.textView.text = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            repeat(4) {
                delay(2000)
                viewModel.flowText.value = "Text update ($it) ${Date()}"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = layout.root

}