package com.example.arcoretest

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.pierluigipapeschi.arcore_app.R


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0

    var arFragment: ArFragment? = null
    var lampPostRenderable: ModelRenderable? = null
    private val selectedObject: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        arFragment!!.setOnTapArPlaneListener { hitresult: HitResult, plane: Plane, motionevent: MotionEvent? ->
            if (plane.getType() !== Plane.Type.HORIZONTAL_UPWARD_FACING) return@setOnTapArPlaneListener
            val anchor = hitresult.createAnchor()
            placeObject(arFragment!!, anchor, Uri.parse("chair.sfb"))
        }

    }

    private fun placeObject(
        arFragment: ArFragment,
        anchor: Anchor,
        uri: Uri
    ) {
        ModelRenderable.builder()
            .setSource(arFragment.context, uri)
            .build()
            .thenAccept { modelRenderable: ModelRenderable? ->
                if (modelRenderable != null) {
                    addNodeToScene(
                        arFragment,
                        anchor,
                        modelRenderable
                    )
                }
            }
            .exceptionally { throwable: Throwable ->
                Toast.makeText(
                    arFragment.context,
                    "Error:" + throwable.message,
                    Toast.LENGTH_LONG
                ).show()
                null
            }
    }

    private fun addNodeToScene(
        arFragment: ArFragment,
        anchor: Anchor,
        renderable: Renderable
    ) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(arFragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        arFragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

}