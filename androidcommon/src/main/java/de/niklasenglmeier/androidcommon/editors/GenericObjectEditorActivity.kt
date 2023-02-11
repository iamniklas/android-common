package de.niklasenglmeier.androidcommon.editors

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import de.niklasenglmeier.androidcommon.R
import de.niklasenglmeier.androidcommon.adapters.SimpleTextLayoutAdapter
import de.niklasenglmeier.androidcommon.adapters.interfaces.AdapterItemClickListener
import de.niklasenglmeier.androidcommon.adapters.spinner.SimpleTextLayoutSpinnerAdapter
import de.niklasenglmeier.androidcommon.extensions.humanize
import java.lang.reflect.Field
import java.lang.reflect.Modifier


class GenericObjectEditorActivity<T : Parcelable> : AppCompatActivity() {

    private lateinit var obj: T

    private lateinit var linearLayout: LinearLayout

    override fun onBackPressed() {
        val data = Intent()
        data.putExtra("object", obj)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_object_editor)

        supportActionBar!!.title = "Generic Object Editor"

        linearLayout = findViewById(R.id.linearLayout_genericEditor)

        obj = intent.getParcelableExtra("object")!!

        val fields = sortFieldsByPriority(obj.javaClass.declaredFields)

        for (field in fields) {
            if(field.isAnnotationPresent(HideFromEditor::class.java)) {
                continue
            }

            when(field.type) {
                String::class.java -> {
                    val v = layoutInflater.inflate(R.layout.editor_field_textinputlayout, null)
                    v.findViewById<TextInputLayout>(R.id.textInputLayout_editor).apply {
                        isEnabled = !field.isAnnotationPresent(NotEditable::class.java)
                        hint = getHintText(field)
                        editText!!.addTextChangedListener(object : TextWatcher {
                            var oldText = ""

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                /*if(field.isAnnotationPresent(FloatValueRange::class.java)) {
                                    oldText = s.toString()
                                }*/
                            }
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                /*if(field.isAnnotationPresent(FloatValueRange::class.java)) {
                                    if(s.toString().toFloat() > field.getAnnotation(FloatValueRange::class.java).maximum) {
                                        editText!!.setText(oldText)
                                    }
                                }*/
                            }

                            override fun afterTextChanged(s: Editable?) {
                                field.isAccessible = true
                                field.set(obj, s.toString())
                                field.isAccessible = false
                            }
                        })


                        field.isAccessible = true
                        if((field.get(obj) as String).isNotEmpty()) {
                            editText!!.setText(field.get(obj)?.toString() ?: "")
                        }
                        field.isAccessible = false
                    }

                    linearLayout.addView(v)
                }

                //TODO Secure implementation of number ranges
                //TODO Implement other primitive types than integer
                Int::class.java,
                    /*UInt::class.java,
                    Long::class.java,
                    ULong::class.java,
                    Byte::class.java,
                    UByte::class.java,
                    Short::class.java,
                    UShort::class.java */ -> {
                    when(field.getAnnotation(NumberEdit::class.java)?.viewType ?: NumberEditType.EditText) {
                        NumberEditType.SeekBar -> {
                            if(!field.isAnnotationPresent(IntValueRange::class.java)) {
                                throw IllegalStateException("Using SeekBar as NumberViewType requires the IntValueRange annotation. To fix this error, add the missing annotation to the variable '${field.name}'")
                            }

                            val intValueRange = field.getAnnotation(IntValueRange::class.java)!!

                            if((intValueRange.maximum - intValueRange.minimum) % intValueRange.step != 0) {
                                throw IllegalArgumentException("The values of the IntValueRange annotation must conform to the rule '(maximum - minimum) % 2 = 0'")
                            }

                            val v = layoutInflater.inflate(R.layout.editor_field_seekbar, null)

                            v.findViewById<SeekBar>(R.id.seekBar_editor).apply {
                                var value: Int
                                isEnabled = getNotEditable(field)
                                val unit = if (field.isAnnotationPresent(UnitDecorator::class.java)) field.getAnnotation(UnitDecorator::class.java)!!.unit.toString() else ""

                                field.isAccessible = true
                                value = field.getInt(obj)
                                v.findViewById<TextView>(R.id.textView_editor_value).text = "${value}${unit}"
                                progress = (value - intValueRange.minimum) / intValueRange.step
                                field.isAccessible = false

                                v.findViewById<TextView>(R.id.textView_editor).text = getHintText(field)

                                max = ((intValueRange.maximum - intValueRange.minimum) / intValueRange.step)

                                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                        value = intValueRange.minimum + (progress * intValueRange.step)
                                        v.findViewById<TextView>(R.id.textView_editor_value).text = "${value}${unit}"

                                        field.isAccessible = true
                                        field.set(obj, value)
                                        field.isAccessible = false
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                                })
                            }

                            linearLayout.addView(v)
                        }

                        NumberEditType.EditText -> {
                            val v = layoutInflater.inflate(R.layout.editor_field_textinputlayout, null)
                            v.findViewById<TextInputLayout>(R.id.textInputLayout_editor).apply {
                                editText!!.inputType = InputType.TYPE_CLASS_NUMBER
                                isEnabled = getNotEditable(field)

                                editText!!.addTextChangedListener(object : TextWatcher {
                                    var oldText = ""

                                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                        /*if(field.isAnnotationPresent(IntValueRange::class.java)) {
                                            oldText = s.toString()
                                        }*/
                                    }
                                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                        /*if(field.isAnnotationPresent(IntValueRange::class.java)) {
                                            if(s.toString().toFloat() > field.getAnnotation(IntValueRange::class.java).maximum) {
                                                editText!!.setText(oldText)
                                            }
                                        }*/
                                    }

                                    override fun afterTextChanged(s: Editable?) {
                                        field.isAccessible = true
                                        field.set(obj, if (s.toString().isNotEmpty()) s.toString().toInt() else 0)
                                        field.isAccessible = false
                                    }
                                })

                                val unit = getUnitText(field)
                                hint = if(unit.isNotEmpty()) {
                                    "${getHintText(field)} (${unit})"
                                } else {
                                    getHintText(field)
                                }

                                field.isAccessible = true
                                editText!!.setText(field.getInt(obj).toString())
                                field.isAccessible = false
                            }

                            linearLayout.addView(v)
                        }
                    }
                }

                Float::class.java,
                Double::class.java -> {
                    when(field.getAnnotation(NumberEdit::class.java)?.viewType ?: NumberEditType.EditText) {
                        NumberEditType.EditText -> {
                            val v = layoutInflater.inflate(R.layout.editor_field_textinputlayout, null)

                            v.findViewById<TextInputLayout>(R.id.textInputLayout_editor).apply {
                                editText!!.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                editText!!.isEnabled = !field.isAnnotationPresent(NotEditable::class.java)

                                val unit = getUnitText(field)
                                hint = if(unit.isNotEmpty()) {
                                    "${getHintText(field)} (${unit})"
                                } else {
                                    getHintText(field)
                                }

                                field.isAccessible = true
                                if(field.type == Float::class.java) {
                                    editText!!.setText(field.getFloat(obj).toString())
                                } else if(field.type == Double::class.java) {
                                    editText!!.setText(field.getDouble(obj).toString())
                                }
                                field.isAccessible = false

                                editText!!.addTextChangedListener(object : TextWatcher {
                                    var oldText = ""

                                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                        /*if(field.isAnnotationPresent(FloatValueRange::class.java)) {
                                            oldText = s.toString()
                                        }*/
                                    }
                                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                        /*if(field.isAnnotationPresent(FloatValueRange::class.java)) {
                                            if(s.toString().toFloat() > field.getAnnotation(FloatValueRange::class.java).maximum) {
                                                editText.setText(oldText)
                                            }
                                        }*/
                                    }

                                    override fun afterTextChanged(s: Editable?) {
                                        field.isAccessible = true
                                        if(field.type == Float::class.java) {
                                            field.set(obj, if (s!!.isNotEmpty()) s.toString().toFloat() else 0.0f)
                                        } else if(field.type == Double::class.java) {
                                            field.set(obj, if (s!!.isNotEmpty()) s.toString().toDouble() else 0.0)
                                        }
                                        field.isAccessible = false
                                    }
                                })
                            }

                            linearLayout.addView(v)
                        }

                        NumberEditType.SeekBar -> {
                            if(!field.isAnnotationPresent(FloatValueRange::class.java)) {
                                throw IllegalStateException("Using SeekBar as NumberViewType requires the FloatValueRange annotation. To fix this error, add the missing annotation to the variable '${field.name}'")
                            }

                            val floatValueRange = field.getAnnotation(FloatValueRange::class.java)!!

                            if((floatValueRange.maximum - floatValueRange.minimum) % floatValueRange.step != 0.0f) {
                                throw IllegalArgumentException("The values of the FloatValueRange annotation must conform to the rule '(maximum - minimum) % 2 = 0'")
                            }

                            val v = layoutInflater.inflate(R.layout.editor_field_seekbar, null)

                            v.findViewById<SeekBar>(R.id.seekBar_editor).apply {

                                isEnabled = getNotEditable(field)
                                val unit = if (field.isAnnotationPresent(UnitDecorator::class.java)) field.getAnnotation(UnitDecorator::class.java)!!.unit.toString() else ""

                                field.isAccessible = true
                                var value = if (field.type == Float::class.java) field.getFloat(obj) else field.getDouble(obj)
                                v.findViewById<TextView>(R.id.textView_editor_value).text = "${value}${unit}"
                                if(field.type == Float::class.java) {
                                    progress = (((value as Float) - floatValueRange.minimum) / floatValueRange.step).toInt()
                                } else {
                                    progress = (((value as Double) - floatValueRange.minimum) / floatValueRange.step).toInt()
                                }
                                field.isAccessible = false

                                v.findViewById<TextView>(R.id.textView_editor).text = getHintText(field)

                                max = ((floatValueRange.maximum - floatValueRange.minimum) / floatValueRange.step).toInt()

                                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                        value = floatValueRange.minimum + (progress * floatValueRange.step)
                                        v.findViewById<TextView>(R.id.textView_editor_value).text = "${value}${unit}"

                                        field.isAccessible = true
                                        field.set(obj, value)
                                        field.isAccessible = false
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                                })
                            }

                            linearLayout.addView(v)
                        }
                    }
                }

                Boolean::class.java -> {
                    val v = when(field.getAnnotation(BooleanEdit::class.java)?.viewType ?: BooleanEditType.CheckBox) {
                        BooleanEditType.CheckBox -> { layoutInflater.inflate(R.layout.editor_field_checkbox, null) }
                        BooleanEditType.ToggleButton -> { layoutInflater.inflate(R.layout.editor_field_togglebutton, null) }
                        BooleanEditType.Switch -> { layoutInflater.inflate(R.layout.editor_field_switch, null) }
                    }

                    when(field.getAnnotation(BooleanEdit::class.java)?.viewType ?: BooleanEditType.CheckBox) {
                        BooleanEditType.CheckBox -> {
                            v.findViewById<CheckBox>(R.id.checkBox_editor).apply {
                                isEnabled = getNotEditable(field)

                                text = getHintText(field)
                                field.isAccessible = true
                                isChecked = field.getBoolean(obj)
                                field.isAccessible = false

                                setOnCheckedChangeListener { _, isChecked ->
                                    field.isAccessible = true
                                    field.setBoolean(obj, isChecked)
                                    field.isAccessible = false
                                }
                            }
                        }
                        BooleanEditType.ToggleButton -> {
                            v.findViewById<ToggleButton>(R.id.toggleButton_editor).apply {
                                if(field.isAnnotationPresent(NotEditable::class.java)) {
                                    isEnabled = false
                                }

                                v.findViewById<TextView>(R.id.textView_editor).text = getHintText(field)

                                if(field.isAnnotationPresent(ToggleButtonTexts::class.java)) {
                                    textOff = field.getAnnotation(ToggleButtonTexts::class.java).textOff
                                    textOn = field.getAnnotation(ToggleButtonTexts::class.java).textOn
                                }

                                field.isAccessible = true
                                isChecked = field.getBoolean(obj)
                                field.isAccessible = false

                                setOnCheckedChangeListener { _, isChecked ->
                                    field.isAccessible = true
                                    field.setBoolean(obj, isChecked)
                                    field.isAccessible = false
                                }
                            }
                        }
                        BooleanEditType.Switch -> {
                            v.findViewById<Switch>(R.id.switch_editor).apply {
                                isEnabled = getNotEditable(field)

                                text = getHintText(field)

                                field.isAccessible = true
                                isChecked = field.getBoolean(obj)
                                field.isAccessible = false

                                setOnCheckedChangeListener { _, isChecked ->
                                    field.isAccessible = true
                                    field.setBoolean(obj, isChecked)
                                    field.isAccessible = false
                                }
                            }
                        }
                    }

                    linearLayout.addView(v)
                }

                SelectionData::class.java -> {
                    val v = layoutInflater.inflate(R.layout.editor_field_selector, null)
                    v.findViewById<TextView>(R.id.textView_editor).text = "${getHintText(field)}"

                    val spinner = v.findViewById<Spinner>(R.id.spinner_editor)

                    if(field.isAnnotationPresent(NotEditable::class.java)) {
                        spinner.isEnabled = false
                        spinner.isClickable = false
                    }

                    field.isAccessible = true
                    val f = field.get(obj) as SelectionData
                    field.isAccessible = false

                    val adapter = SimpleTextLayoutSpinnerAdapter(this, f.items, R.layout.spinner_item)
                    adapter.setDropDownViewResource(R.layout.spinner_item)

                    spinner.adapter = adapter

                    linearLayout.addView(v)
                }

                else -> {
                    //Enum Types
                    if(field.type.superclass == Enum::class.java) {
                        val editType = field.getAnnotation(EnumEdit::class.java)?.viewType ?: EnumEditType.RadioButton
                        when(editType)  {
                            EnumEditType.RadioButton -> {
                                val v = layoutInflater.inflate(R.layout.editor_field_radiogroup, null)

                                val radioGroup = v.findViewById<RadioGroup>(R.id.radioGroup_editor)

                                v.findViewById<TextView>(R.id.textView_editor).text = getHintText(field)

                                for (i in 0 until field.type.enumConstants.size) {
                                    val vRb = layoutInflater.inflate(R.layout.editor_field_radiobutton, null)
                                    val rb = vRb.findViewById<RadioButton>(R.id.radioButton_editor)
                                    rb.text = field.type.enumConstants[i].toString()

                                    rb.id = View.generateViewId()

                                    field.isAccessible = true
                                    if(field.type.enumConstants[i].toString() == field.get(obj).toString()) {
                                        rb.isChecked = true
                                    }
                                    field.isAccessible = false

                                    rb.setOnClickListener {
                                        val enumType = Class.forName(field.type.canonicalName)
                                        val enumConstant = enumType.getField((it as RadioButton).text.toString())[null]

                                        field.isAccessible = true
                                        field.set(obj, enumConstant)
                                        field.isAccessible = false
                                    }

                                    rb.isEnabled = getNotEditable(field)

                                    radioGroup.addView(rb)
                                }


                                linearLayout.addView(v)
                            }
                            EnumEditType.Spinner -> {
                                val v = layoutInflater.inflate(R.layout.editor_field_selector, null)

                                val spinner = v.findViewById<Spinner>(R.id.spinner_editor)

                                v.findViewById<TextView>(R.id.textView_editor).text = getHintText(field)

                                if(field.isAnnotationPresent(NotEditable::class.java)) {
                                    spinner.isEnabled = false
                                    spinner.isClickable = false
                                }

                                var list = mutableListOf<SimpleTextLayoutSpinnerAdapter.DataItem>()
                                for (i in field.type.enumConstants) {
                                    list.add(SimpleTextLayoutSpinnerAdapter.DataItem(i.toString()))
                                }

                                val adapter = SimpleTextLayoutSpinnerAdapter(this, list.toTypedArray(), R.layout.spinner_item)

                                v.findViewById<TextView>(R.id.textView_editor).text = "${getHintText(field)}"

                                spinner.adapter = adapter

                                field.isAccessible = true
                                spinner.setSelection(list.indexOfFirst { it.text == field.get(obj).toString() })
                                field.isAccessible = false

                                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {
                                        field.isAccessible = true
                                        field.set(obj, field.type.enumConstants[position])
                                        field.isAccessible = false
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) { }
                                }

                                linearLayout.addView(v)
                            }
                        }
                    }
                }
            }
        }
    }

    fun sortFieldsByPriority(fields: Array<Field>): Array<Field> {
        var f = mutableListOf<Field>()
        var priorities = mutableListOf<Int>()

        var nonStaticFields = fields.filter { !Modifier.isStatic(it.modifiers) }.toMutableList()

        for (field in nonStaticFields) {
            priorities.add(field.getAnnotation(Priority::class.java)?.priority ?: 999)
        }

        for (i in 0 until priorities.size - 1) {
            for (j in 0 until priorities.size - i - 1) {
                if (priorities[j] > priorities[j + 1]) {
                    val temp = priorities[j]
                    val tempObj = nonStaticFields[j]

                    priorities[j] = priorities[j + 1]
                    nonStaticFields[j] = nonStaticFields[j + 1]

                    priorities[j + 1] = temp
                    nonStaticFields[j + 1] = tempObj
                }
            }
        }

        return nonStaticFields.toTypedArray()
    }

    fun getHintText(field: Field) : String { return if (field.isAnnotationPresent(HintTextResource::class.java)) getString(field.getAnnotation(HintTextResource::class.java).resourceId) else field.name.humanize() }
    fun getUnitText(field: Field) : String { return if (field.isAnnotationPresent(UnitDecorator::class.java)) field.getAnnotation(UnitDecorator::class.java).unit.toString() else "" }
    fun getNotEditable(field: Field): Boolean { return !field.isAnnotationPresent(NotEditable::class.java) }

    companion object {
        fun <T : Parcelable> createIntent(context: Context, obj: T) : Intent {
            val i = Intent(context, GenericObjectEditorActivity::class.java)
            i.putExtra("object", obj)
            return i
        }
    }
}