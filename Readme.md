# RxUtilities

![](https://img.shields.io/badge/version-0.0.1-blue.svg) ![](https://travis-ci.org/joemccann/dillinger.svg?branch=master) 

RxUtilities contained rx-wrapped utils such as simplifying permission request, throttling click action, accessing local data, etc.

### Installation
TODO : Will publish this library to public maven repository

### Available Utils
Here are current RxUtilities available so far

#### 1. RxPermission
Simplifying permission request implementation by utilise rxjava observable. Support  permission contract for android SDK >= 23 and < 23.
Permission rationale dialog is supported for SDK >= 23.
##### How to use
Below is the sample code to show how to use the `RxPermission`
```
class RxPermissionActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_permission)

        val requestPermission = listOf(
            RxPermissionRequest(Manifest.permission.CAMERA),
            RxPermissionRequest(Manifest.permission.READ_CONTACTS)
        )

        val permission = RxPermission()
        val subscribed =
            permission.request(requestPermission, this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        when (result) {
                            is Granted -> {
                                Log.d("RX_PERMISSION", "The result is ${result.data}")
                            }
                            is Denied -> {
                                Log.d(
                                    "RX_PERMISSION",
                                    "Permission denied ${result.exception.message}"
                                )
                            }
                            is Failed -> {
                                Log.d("RX_PERMISSION", "Some error occurred")
                            }
                        }
                    }, {
                        Log.d("RX_PERMISSION", "Throws ${it.message}")
                    })

        disposable.add(subscribed)
    }
}
```
#### 2. Rx Click Event
Rx click event consist of 2 types: throttleClick, and debounceClick.
`throttleClick` will emit click event, invoke the first emitted one, and will do another invocation afterthe first one completed and passing the specified time window.
`debounceClick` will emit click event, and only able to do other invocation after passing the timeout.
#### How to Use
This code below is how to use throttleClick and debounceClick.
```
btn_rx_throttle.clickThrottle {
    Log.d("EVENT_CLICK", "Clicked emitted at ${Date().time}")
}

btn_rx_debounce.clickDebounce {
    Log.d("EVENT_CLICK", "Clicked emitted at ${Date().time}")
}
```
