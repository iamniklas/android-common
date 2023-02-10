package de.niklasenglmeier.androidcommon.editors


/**
 * If this annotation is set, the specified field will be shown on top in the order of priority
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Priority(val priority: Int)

/**
 * If this annotation is set,
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class HintTextResource(val resourceId: Int)

/**
 * The specified field is not shown in the editor and is not editable by the user
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class HideFromEditor

/**
 * The specified field is shown in the editor, but is not editable by the user
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class NotEditable

/**
 * @param minimum The minimum allowed value for the specified field
 * @param maximum The maximum allowed value for the specified field
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IntValueRange(val minimum: Int, val maximum: Int, val step: Int)

/**
 * @param minimum The minimum allowed value for the specified field
 * @param maximum The maximum allowed value for the specified field
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class FloatValueRange(val minimum: Float, val maximum: Float, val step: Float)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class DoubleValueRange(val minimum: Double, val maximum: Double, val step: Double)

/**
 * @param maximum The maximum allowed text length for the specified field
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class MaxTextLength(val maximum: Int)

/**
 * Use Slider editing for the specified field (number field types int, float)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class NumberEdit(val viewType: NumberEditType)

/**
 * Use InputField editing for the specified field (int, float, string)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class InputFieldEdit()

/**
 * Use InputField editing for the specified field (int, float, string)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ToggleButtonTexts(val textOff: String, val textOn: String)

/**
 * Use ToggleButton editing for the specified field (boolean)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class BooleanEdit(val viewType: BooleanEditType)

/**
 * Use ToggleButton editing for the specified field (boolean)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class EnumEdit(val viewType: EnumEditType)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Required

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class UnitDecorator(val unit: Unit)

enum class NumberEditType {
    EditText, SeekBar
}
enum class EnumEditType {
    RadioButton, Spinner
}
enum class BooleanEditType {
    CheckBox, ToggleButton, Switch
}

enum class Unit {
    //Area
    ac, a, ha, cm2, ft2, in2, m2,

    //Temperature
    c, f, k,

    //Speed
    m_s, m_h, km_s, km_h, in_s, in_h, ft_s, ft_h, mi_s, mi_h, kn,

    //Volume
    gal, l, ml, cm3, m3, in3, ft3,

    //Data
    bit, B, KB, MB, GB, TB,

    //Mass
    kg, g, mg, lbs, t, oz,

    //Length
    km, m, cm, mm, mi, ft, `in`, yd, NM, mil,

    //Duration
    ms, s, min, h, d, wk, y
}