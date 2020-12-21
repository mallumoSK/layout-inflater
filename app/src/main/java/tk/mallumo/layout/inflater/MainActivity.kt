@file:Suppress("DEPRECATION")

package tk.mallumo.layout.inflater

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class VM : ViewModel() {
    val namexyz = MutableStateFlow("flowInitValue")
}

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    val vm by lazy {
        ViewModelProviders.of(this)[VM::class.java]
    }

    val layout by lazyLayout<LayoutActivityMain> {
        incLayout.veverka.text = "Included layout after inflate"

//        flow(vm.namexyz) { veverka.text = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.root)

        lifecycleScope.launchWhenResumed {
            delay(5000)
            vm.namexyz.value = "Value after delay ${Date()}"
            delay(2000)
            vm.namexyz.value = "Next flow value ${Date()}"
        }
    }
}