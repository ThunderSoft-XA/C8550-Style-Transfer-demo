package org.tensorflow.lite.examples.styletransfer.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ImageProxy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import org.tensorflow.lite.examples.styletransfer.R
import kotlinx.android.synthetic.main.fragment_select_image.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.styletransfer.MainViewModel
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


class SelectImageFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()


    companion object {
        private const val REQUEST_CODE_GALLERY = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        select_image_button.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        println("选择图片：")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            // 进行后续操作，例如显示选择的图片或上传到服务器等
            println("选择的图片是：" + selectedImageUri)
            if (selectedImageUri != null) {
                // 创建一个字节缓冲区
                GlobalScope.launch(Dispatchers.IO) {
                    val inputStream =
                        requireContext().contentResolver.openInputStream(selectedImageUri)
                    val bufferedInputStream = BufferedInputStream(inputStream)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bufferedInputStream.use { input ->
                        byteArrayOutputStream.use { output ->
                            val buffer = ByteArray(1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    val byteBuffer: ByteBuffer =
                        ByteBuffer.wrap(byteArrayOutputStream.toByteArray())

                    // 在这里使用字节缓冲区 byteBuffer
                    withContext(Dispatchers.Main) {
                        // 处理 byteBuffer
                        println("设置viewModel")
                        viewModel.setInputImage(
                            byteBuffer,
                            0
                        )
                        viewModel.inputBitmap.observe(viewLifecycleOwner) {
                            Navigation.findNavController(
                                requireActivity(),
                                R.id.fragment_container
                            ).navigate(
                                SelectImageFragmentDirections.actionSelectImageFragmentToTransformationFragment()
                            )
                        }
                    }
                }
            }


        }
    }
}