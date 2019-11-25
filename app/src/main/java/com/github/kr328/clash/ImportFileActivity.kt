package com.github.kr328.clash

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.kr328.clash.model.ClashProfile
import com.github.kr328.clash.service.data.ClashProfileEntity
import com.github.kr328.clash.utils.FileUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_import_file.*
import java.io.FileOutputStream
import kotlin.concurrent.thread

class ImportFileActivity : BaseActivity() {
    companion object {
        const val REQUEST_FILE_CODE = 185
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_file)

        setSupportActionBar(activity_import_file_toolbar)

        activity_import_file_save.setOnClickListener {
            when {
                activity_import_file_name_text.text.isEmpty() -> {
                    Snackbar.make(
                        activity_import_file_root,
                        R.string.clash_import_file_empty_name,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                activity_import_file_path_text.tag == null -> {
                    Snackbar.make(
                        activity_import_file_root,
                        R.string.clash_import_file_empty_path,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                    it.visibility = View.GONE
                    activity_import_file_saving.visibility = View.VISIBLE

                    thread {
                        checkAndInsert()

                        runOnUiThread {
                            it.visibility = View.VISIBLE
                            activity_import_file_saving.visibility = View.GONE
                        }
                    }
                }
            }
        }

        val name = activity_import_file_name_text

        activity_import_file_name.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clash_profile_name)
                .setView(R.layout.dialog_text_edit)
                .setPositiveButton(R.string.ok) { _, _ -> }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .create()
                .apply {
                    setOnShowListener {
                        val data = findViewById<EditText>(R.id.dialog_text_edit).also {
                            it?.hint = name.hint
                            it?.setText(name.text)
                        }
                        getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                            setTextColor(getColor(R.color.colorAccent))
                            setOnClickListener {
                                name.text = data?.text ?: ""
                                dismiss()
                            }
                        }
                        getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                            setTextColor(getColor(R.color.colorAccent))
                        }
                    }
                }
                .show()
        }

        activity_import_file_path.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_GET_CONTENT)
                    .setType(
                        MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension("yaml") ?: "*/*"
                    ),
                REQUEST_FILE_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FILE_CODE && resultCode == Activity.RESULT_OK) {
            activity_import_file_path_text.tag = data?.data
            activity_import_file_path_text.text = data?.data?.pathSegments?.last() ?: ""
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkAndInsert() {
        try {
            val data  = contentResolver.openInputStream(activity_import_file_path_text.tag as Uri)?.use {
                it.readBytes().toString(Charsets.UTF_8)
            } ?: throw NullPointerException("Unable to open config file")

            val parsed = Yaml(configuration = YamlConfiguration(strictMode = false)).parse(ClashProfile.serializer(), data)
            val cache = FileUtils.generateRandomFile(filesDir.resolve(Constants.PROFILES_DIR), ".yaml")

            FileOutputStream(cache).use {
                it.write(data.toByteArray())
            }

            runClash {
                it.profileService.addProfile(
                    ClashProfileEntity(
                        name = activity_import_file_name_text.text.toString(),
                        token = "file|" + activity_import_file_path_text.tag.toString(),
                        cache = cache.absolutePath,
                        active = false,
                        proxies = parsed.proxies.size,
                        proxyGroups = parsed.proxyGroups.size,
                        rules = parsed.rules.size,
                        lastUpdate = System.currentTimeMillis()
                    )
                )
            }

            finish()
        }
        catch (e: Exception) {
            Snackbar.make(
                activity_import_file_root,
                getString(R.string.clash_import_file_invalid, e.toString()),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}