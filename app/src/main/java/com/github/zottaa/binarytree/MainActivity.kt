package com.github.zottaa.binarytree

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.github.zottaa.binarytree.BinaryTree.Base


class MainActivity : ComponentActivity() {

    private var binaryTree = Base()
    private val userFactory = UserFactory()
    private var builder: UserType = userFactory.getBuilderByName(userFactory.typeNameList[0])

    private lateinit var addValueField: EditText
    private lateinit var deleteValueField: EditText
    private lateinit var atValueField: EditText
    private lateinit var addButton: Button
    private lateinit var deleteButton: Button
    private lateinit var atButton: Button
    private lateinit var balanceButton: Button
    private lateinit var serializeButton: Button
    private lateinit var deserializeButton: Button
    private lateinit var showButton: Button
    private lateinit var traverseOrderButton: Button
    private lateinit var clearButton: Button
    private lateinit var outputArea: TextView
    private lateinit var operationSpinner: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addValueField = findViewById(R.id.inputAdd)
        deleteValueField = findViewById(R.id.inputDelete)
        atValueField = findViewById(R.id.inputAt)
        addButton = findViewById(R.id.addButton)
        deleteButton = findViewById(R.id.deleteButton)
        atButton = findViewById(R.id.atButton)
        balanceButton = findViewById(R.id.balanceButton)
        serializeButton = findViewById(R.id.serializeButton)
        deserializeButton = findViewById(R.id.deserializeButton)
        showButton = findViewById(R.id.showButton)
        traverseOrderButton = findViewById(R.id.traverseOrderButton)
        clearButton = findViewById(R.id.clearButton)
        outputArea = findViewById(R.id.mainText)
        operationSpinner = findViewById(R.id.typeSpinner)

        addButton.setOnClickListener { onAddButtonClicked() }
        deleteButton.setOnClickListener { onDeleteButtonClicked() }
        atButton.setOnClickListener { onAtButtonClicked() }
        balanceButton.setOnClickListener { onBalanceButtonClicked() }
        serializeButton.setOnClickListener { onSerializeButtonClicked() }
        deserializeButton.setOnClickListener { onDeserializeButtonClicked() }
        showButton.setOnClickListener { onUpdateOutputClicked() }
        traverseOrderButton.setOnClickListener { onTraverseOrderButtonClicked() }
        clearButton.setOnClickListener { onClearButtonClicked() }

        val items = userFactory.typeNameList
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        operationSpinner.adapter = adapter
        operationSpinner.setSelection(0)
        operationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val oldValue = builder.typeName()
                builder = userFactory.getBuilderByName(items[position])
                addValueField.hint = "Enter value: example ${builder.create()}"
                if (!oldValue.equals(builder.typeName(), ignoreCase = true)) {
                    binaryTree.clear()
                    updateOutput()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun onClearButtonClicked() {
        binaryTree.clear()
        updateOutput()
    }

    private fun onTraverseOrderButtonClicked() {
        val stringBuilder = StringBuilder()
        binaryTree.forEach { v: UserType ->
            stringBuilder.append(v.toString())
            stringBuilder.append(" ")
        }
        outputArea.text = stringBuilder.toString()
    }

    private fun onUpdateOutputClicked() {
        updateOutput()
    }

    private fun onDeserializeButtonClicked() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"

        fileOpenLauncher.launch(intent)
    }

    private val fileOpenLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    val serialize = Serialize.Base()
                    binaryTree = serialize.deserialize(
                        uri,
                        applicationContext.contentResolver
                    ) as Base
                    if (!binaryTree.isEmpty) {
                        builder = userFactory.getBuilderByName(binaryTree.at(0).typeName())
                        val index = userFactory.typeNameList.indexOf(builder.typeName())
                        operationSpinner.setSelection(index)
                    }
                    updateOutput()
                }

            }
        }

    private val fileSaveLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    val serialize = Serialize.Base()
                    serialize.serialize(
                        binaryTree,
                        uri,
                        builder.typeName(),
                        applicationContext.contentResolver
                    )
                }
            }
        }

    private fun onSerializeButtonClicked() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "example.txt")

        fileSaveLauncher.launch(intent)
    }

    private fun onBalanceButtonClicked() {
        binaryTree.balance()
        updateOutput()
    }

    private fun onAtButtonClicked() {
        if (atValueField.text.isNotEmpty()) {
            try {
                val index = atValueField.text.toString().toInt()
                val value = binaryTree.at(index).toString()
                outputArea.text = value
            } catch (e: IllegalArgumentException) {
                showAlert("Invalid input", "Invalid input. Please enter a valid value.")
            }
        }
    }

    private fun onDeleteButtonClicked() {
        if (deleteValueField.text.isNotEmpty()) {
            try {
                val index = deleteValueField.text.toString().toInt()
                binaryTree.delete(index)
                updateOutput()
            } catch (e: IllegalArgumentException) {
                showAlert("Invalid input", "Invalid input. Please enter a valid value.")
            }
        }
    }

    private fun onAddButtonClicked() {
        if (addValueField.text.isNotEmpty()) {
            try {
                val value = builder.parseValue(addValueField.text.toString()) as UserType
                binaryTree.add(value)
                updateOutput()
            } catch (e: IllegalArgumentException) {
                showAlert("Invalid input", "Invalid input. Please enter a valid value.")
            }
        }
    }

    private fun updateOutput() {
        outputArea.text = binaryTree.toString()
    }

    private fun showAlert(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)

        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}