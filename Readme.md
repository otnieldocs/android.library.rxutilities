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

```
class RxPermissionActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_permission)

        val requestPermission = listOf(
            RxPermissionRequest(
                permission = Manifest.permission.CAMERA,
                rationaleMessage = "You need to allow camera permission to use this feature"
            ),
            RxPermissionRequest(
                permission = Manifest.permission.READ_CONTACTS,
                rationaleMessage = "You need to allow camera permission to use this feature"
            )
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