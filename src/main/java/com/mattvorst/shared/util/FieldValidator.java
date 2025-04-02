package com.mattvorst.shared.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mattvorst.shared.constant.ValidationLimits;
import com.mattvorst.shared.exception.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.validation.FieldError;

public class FieldValidator {
	private MessageSource messageSource;
	private Locale locale;
	private ArrayList<FieldError> fieldErrorList;

	private static final Logger log = LogManager.getLogger(FieldValidator.class);

	public FieldValidator(MessageSource messageSource, Locale locale, ArrayList<FieldError> fieldErrorList) {
		this.messageSource = messageSource;
		this.locale = locale;
		this.fieldErrorList = fieldErrorList;
	}

	public static FieldValidator get(MessageSource messageSource, Locale locale) {
		return new FieldValidator(messageSource, locale, new ArrayList<>());
	}

	public static FieldValidator get(MessageSource messageSource, Locale locale, ArrayList<FieldError> fieldErrorList) {
		return new FieldValidator(messageSource, locale, fieldErrorList);
	}

	public ArrayList<FieldError> getFieldErrorList() {
		return this.fieldErrorList;
	}

	public FieldValidator withCustomFieldError(String fieldName, String errorCode, Object[] errorArguments) {
		String message = messageSource.getMessage(errorCode, errorArguments, locale);
		fieldErrorList.add(new FieldError("", fieldName, message));
		return this;
	}

	public FieldValidator withCustomFieldError(String fieldName, String errorCode) {
		return withCustomFieldError(fieldName, errorCode, new Object[]{});
	}

	public void apply() throws ValidationException {
		if(fieldErrorList.size() > 0){
			throw new ValidationException("Please fix the form and try again.", fieldErrorList);
		}
	}

	public FieldValidator validateAfter(String fieldName, String fieldInstanceName, Date date, Date date2) {
		if (date != null && date2 != null) {
			if (!date2.after(date)) {
				String message = messageSource.getMessage("error." + fieldName + ".before", new Object[] { date.toString() }, locale);
				fieldErrorList.add(new FieldError("", fieldInstanceName, message));
			}
		}

		return this;
	}

	public FieldValidator validateStringLength(String fieldName, String value, ValidationLimits validationLimits){
		FieldValidator.validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
		return this;
	}

	public FieldValidator validateStringLength(String fieldName, String value, ValidationLimits validationLimits, Supplier<Boolean> conditional){
		if (conditional.get()) {
			FieldValidator.validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
		}

		return this;
	}

	public FieldValidator validateMarkupLength(String fieldName, String value, ValidationLimits validationLimits){
		FieldValidator.validateMarkupLength(messageSource, locale, fieldErrorList, fieldName, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
		return this;
	}

	public FieldValidator validateStringLength(String fieldInstanceName, String fieldName, String value, ValidationLimits validationLimits){
		FieldValidator.validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldInstanceName, value, validationLimits.getMin(), validationLimits.getMax());
		return this;
	}

	public FieldValidator validateStringLength(String fieldName, String value, int minLenght, int maxLength){
		FieldValidator.validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldName, value, minLenght, maxLength);
		return this;
	}

	public FieldValidator validateStringLength(String fieldName, String fieldInstanceName, String value, int minLength, int maxLength){
		FieldValidator.validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldInstanceName, value, minLength, maxLength);
		return this;
	}

	public FieldValidator validateStringLength(String fieldName, String fieldInstanceName, String value, int minLength, int maxLength, Supplier<Boolean> conditional){
		if (conditional.get()) {
			FieldValidator.validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldInstanceName, value, minLength, maxLength);
		}

		return this;
	}

	public FieldValidator validateStringLength(String fieldName, String fieldInstanceName, String value, ValidationLimits validationLimits, Supplier<Boolean> conditional){
		return validateStringLength(fieldName, fieldInstanceName, value, validationLimits.getMin(), validationLimits.getMax(), conditional);
	}

	public FieldValidator validateContains(String fieldName, String fieldInstanceName, Object value, List<Object> acceptableValues) {
		FieldValidator.validateContains(messageSource, locale, fieldErrorList, fieldInstanceName, fieldName, value, acceptableValues);
		return this;
	}

	public FieldValidator validateContains(String fieldName, Object value, List<Object> acceptableValues) {
		FieldValidator.validateContains(messageSource, locale, fieldErrorList, fieldName, fieldName, value, acceptableValues);
		return this;
	}

	public FieldValidator validateContains(String fieldName, Object value, List<Object> acceptableValues, Supplier<Boolean> conditional) {
		if (conditional.get()) {
			FieldValidator.validateContains(messageSource, locale, fieldErrorList, fieldName, fieldName, value, acceptableValues);
		}
		return this;
	}

	public FieldValidator validateNumber(String fieldInstanceName, String fieldName, long value, ValidationLimits validationLimits){
		FieldValidator.validateNumber(messageSource, locale, fieldErrorList, fieldInstanceName, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
		return this;
	}

	public FieldValidator validateNumber(String fieldName, long value, ValidationLimits validationLimits){
		FieldValidator.validateNumber(messageSource, locale, fieldErrorList, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
		return this;
	}

	public FieldValidator validateNumber(String fieldName, long value, int min, int max, Supplier<Boolean> conditional) {
		if (conditional.get()) {
			FieldValidator.validateNumber(messageSource, locale, fieldErrorList, fieldName, fieldName, value, min, max);
		}
		return this;
	}

	public FieldValidator validateNumber(String fieldName, long value, int min, int max){
		FieldValidator.validateNumber(messageSource, locale, fieldErrorList, fieldName, fieldName, value, min, max);
		return this;
	}

	public FieldValidator validateNumber(String fieldInstanceName, String fieldName, long value, int min, int max) {
		FieldValidator.validateNumber(messageSource, locale, fieldErrorList, fieldInstanceName, fieldName, value, min, max);
		return this;
	}

	public FieldValidator validateNotEmpty(String fieldName, String fieldInstanceName, String value){
		FieldValidator.validateNotEmpty(messageSource, locale, fieldErrorList, fieldInstanceName, fieldName, value);
		return this;
	}

	public FieldValidator validateNotEmpty(String fieldName, String value){
		FieldValidator.validateNotEmpty(messageSource, locale, fieldErrorList, fieldName, value);
		return this;
	}


	public FieldValidator validateNotEmpty(String fieldName, Collection<?> value){
		FieldValidator.validateNotEmpty(messageSource, locale, fieldErrorList, fieldName, value);
		return this;
	}

	public FieldValidator validateNotEmpty(String fieldName, String value, Supplier<Boolean> conditional){
		if (conditional.get()) {
			FieldValidator.validateNotEmpty(messageSource, locale, fieldErrorList, fieldName, value);
		}

		return this;
	}

	public FieldValidator validateNotNull(String fieldName, Object value){
		FieldValidator.validateNotNull(messageSource, locale, fieldErrorList, fieldName, value);

		return this;
	}

	public FieldValidator validateNotNull(String fieldInstanceName, String fieldName, Object value){
		FieldValidator.validateNotNull(messageSource, locale, fieldErrorList, fieldInstanceName, fieldName, value);
		return this;
	}

	public FieldValidator validateNotNull(String fieldName, Object value, Supplier<Boolean> conditional){
		if (conditional.get()) {
			FieldValidator.validateNotNull(messageSource, locale, fieldErrorList, fieldName, value);
		}

		return this;
	}

	public FieldValidator validateEmailAddress(String fieldName, String value, ValidationLimits validationLimits){
		FieldValidator.validateEmailAddress(messageSource, locale, fieldErrorList, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
		return this;
	}

	public FieldValidator validateEmailAddress(String fieldName, String fieldInstanceName, String value, ValidationLimits validationLimits, Supplier<Boolean> conditional) {
		if (conditional.get()) {
			FieldValidator.validateEmailAddress(messageSource, locale, fieldErrorList, fieldName, fieldInstanceName, value, validationLimits.getMin(), validationLimits.getMax());
		}

		return this;
	}

	public FieldValidator validateEmailAddress(String fieldName, String fieldInstanceName, String value, ValidationLimits validationLimits) {
		FieldValidator.validateEmailAddress(messageSource, locale, fieldErrorList, fieldName, fieldInstanceName, value, validationLimits.getMin(), validationLimits.getMax());

		return this;
	}

	public FieldValidator validateEmailAddress(String fieldName, String value, ValidationLimits validationLimits, Supplier<Boolean> conditional) {
		if (conditional.get()) {
			FieldValidator.validateEmailAddress(messageSource, locale, fieldErrorList, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
		}

		return this;
	}

	public FieldValidator validateEmailAddress(String fieldName, String value, int minLength, int maxLength){
		FieldValidator.validateEmailAddress(messageSource, locale, fieldErrorList, fieldName, value, minLength, maxLength);
		return this;
	}

	public FieldValidator validateRegex(String fieldName, String value, String regex) {
		FieldValidator.validateRegex(messageSource, locale, fieldErrorList, fieldName, value, regex);
		return this;
	}

	public FieldValidator validateRegex(String fieldName, String value, String regex, Supplier<Boolean> conditional) {
		if (conditional.get()) {
			FieldValidator.validateRegex(messageSource, locale, fieldErrorList, fieldName, value, regex);
		}

		return this;
	}

	public FieldValidator validateBoolean(String fieldName, boolean value) {
		FieldValidator.validateBoolean(messageSource, locale, fieldErrorList, fieldName, fieldName, value);

		return this;
	}

	public FieldValidator validateBoolean(String fieldName, boolean value, Supplier<Boolean> conditional) {
		if (conditional.get()) {
			FieldValidator.validateBoolean(messageSource, locale, fieldErrorList, fieldName, fieldName, value);
		}

		return this;
	}

	public FieldValidator validateRegexValid(String fieldName, String regex) {
		FieldValidator.validateRegexValid(messageSource, locale, fieldErrorList, fieldName, regex);
		return this;
	}

	public FieldValidator validateStringEqual(String fieldName, String val0, String val1) {
		FieldValidator.validateStringEqual(messageSource, locale, fieldErrorList, fieldName, val0, val1);
		return this;
	}

	public FieldValidator validateStringEqual(String fieldName, String val0, String val1, Supplier<Boolean> conditional) {
		if (conditional.get()) {
			FieldValidator.validateStringEqual(messageSource, locale, fieldErrorList, fieldName, val0, val1);
		}

		return this;
	}

	public static void validateStringLength(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String value, ValidationLimits validationLimits){
		validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
	}

	public static void validateStringLength(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, String value, ValidationLimits validationLimits){
		validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldInstanceName, value, validationLimits.getMin(), validationLimits.getMax());
	}

	public static void validateStringLength(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String value, int minLenght, int maxLength){
		validateStringLength(messageSource, locale, fieldErrorList, fieldName, fieldName, value, minLenght, maxLength);
	}

	public static void validateStringLength(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String fieldInstanceName, String value, int minLenght, int maxLength){

		if(value == null){ value = ""; }

		int length = value.trim().length();

		if(minLenght > 0 && length < minLenght){
			String message = messageSource.getMessage("error." + fieldName + ".short", new Object[]{length, minLenght, maxLength}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
		else if(maxLength > 0 && length > maxLength){
			String message = messageSource.getMessage("error." + fieldName + ".long", new Object[]{length, minLenght, maxLength}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
	}

	public static void validateMarkupLength(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String fieldInstanceName, String value, int minLenght, int maxLength){

		value = MarkupUtils.stripMarkup(value);
		if(value == null){
			value = "";
		}

		int length = value.trim().length();

		if(minLenght > 0 && length < minLenght){
			String message = messageSource.getMessage("error." + fieldName + ".short", new Object[]{length, minLenght, maxLength}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
		else if(maxLength > 0 && length > maxLength){
			String message = messageSource.getMessage("error." + fieldName + ".long", new Object[]{length, minLenght, maxLength}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
	}

	public static void validateContains(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, Object value, List<Object> acceptableValues) {
		boolean valid = false;

		if (value == null) {
			valid = true;
		} else if (!Utils.empty(acceptableValues)) {
			if (acceptableValues.contains(value)) {
				valid = true;
			}
		}

		if (!valid) {
			String message = messageSource.getMessage("error." + fieldName + ".contains", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
	}

	public static void validateNumber(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, long value, ValidationLimits validationLimits){
		validateNumber(messageSource, locale, fieldErrorList, fieldInstanceName, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
	}

	public static void validateNumber(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, long value, ValidationLimits validationLimits){
		validateNumber(messageSource, locale, fieldErrorList, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
	}

	public static void validateNumber(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, long value, int min, int max){
		validateNumber(messageSource, locale, fieldErrorList, fieldName, fieldName, value, min, max);
	}

	public static void validateNumber(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, long value, int min, int max){
		if(value < min){
			String message = messageSource.getMessage("error." + fieldName + ".low", new Object[]{value, min, max}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
		else if(value > max){
			String message = messageSource.getMessage("error." + fieldName + ".high", new Object[]{value, min, max}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
	}

	public static void validateNotEmpty(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String value){

		if(Utils.empty(value)){
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldName, message));
		}
	}

	public static void validateNotEmpty(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, String value){

		if(Utils.empty(value)){
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
	}

	public static void validateNotEmpty(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, Collection<?> value){

		if(Utils.empty(value)){
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldName, message));
		}
	}

	public static void validateNotNull(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, Object value){

		if(value == null){
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldName, message));
		}
	}

	public static void validateNotNull(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, Object value){

		if(value == null){
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
	}

	public static void validateBoolean(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, boolean value){
		if (!value) {
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
	}

	public static <T extends Enum<T>> boolean validateEnum(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, T value, Class<T> c){

		boolean valid = true;
		if(value == null){
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldName, message));
			valid = false;
		}else {
			try {
				Enum.valueOf(c, value.toString());
			}catch(Throwable t) {
				String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
				fieldErrorList.add(new FieldError("", fieldName, message));
				valid = false;
			}
		}

		return valid;
	}

	public static <T extends Enum<T>> boolean validateEnum(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldInstanceName, String fieldName, T value, Class<T> c){

		boolean valid = true;
		if(value == null){
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
			valid = false;
		}else {
			try {
				Enum.valueOf(c, value.toString());
			}catch(Throwable t) {
				String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
				fieldErrorList.add(new FieldError("", fieldInstanceName, message));
				valid = false;
			}
		}

		return valid;
	}

	public static void validateEmailAddress(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String value, ValidationLimits validationLimits){
		validateEmailAddress(messageSource, locale, fieldErrorList, fieldName, value, validationLimits.getMin(), validationLimits.getMax());
	}

	public static void validateEmailAddress(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String value, int minLength, int maxLength){
		validateEmailAddress(messageSource, locale, fieldErrorList, fieldName, fieldName, value, minLength, maxLength);
	}

	public static void validateEmailAddress(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String fieldInstanceName, String value,
											int minLength, int maxLength) {

		if(value == null){ value = ""; }

		int length = value.trim().length();

		if(minLength == 0 && length == 0){
			// NOOP
		}else if(minLength > 0 && length < minLength){
			String message = messageSource.getMessage("error." + fieldName + ".short", new Object[]{length, minLength, maxLength}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
		else if(maxLength > 0 && length > maxLength){
			String message = messageSource.getMessage("error." + fieldName + ".long", new Object[]{length, minLength, maxLength}, locale);
			fieldErrorList.add(new FieldError("", fieldInstanceName, message));
		}
		else{
			//	Pattern pattern = Pattern.compile("[A-Z0-9a-z\\._%\\+\\-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,63}");
			Pattern pattern = Pattern.compile(Utils.EMAIL_ADDRESS_REGEX);
			Matcher matcher = pattern.matcher(value.toLowerCase().trim());
			if(!matcher.matches()){
				String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{length, minLength, maxLength}, locale);
				fieldErrorList.add(new FieldError("", fieldInstanceName, message));
			}
		}
	}

	public static void validateRegex(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String value, String regex){

		if(value == null){ value = ""; }

		if(!Utils.empty(regex)){
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(value);
			if(!matcher.matches()){
				String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
				fieldErrorList.add(new FieldError("", fieldName, message));
			}
		}
	}

	public static void validateRegexValid(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String regex){
		try {
			Pattern pattern = Pattern.compile(regex);

		}catch(Throwable t) {
			String message = messageSource.getMessage("error." + fieldName + ".invalid", new Object[]{}, locale);
			fieldErrorList.add(new FieldError("", fieldName, message));
		}
	}

	public static void validateStringEqual(MessageSource messageSource, Locale locale, List<FieldError> fieldErrorList, String fieldName, String val0, String val1){

		if(val0 == null){ val0 = ""; }

		if(!val0.equals(val1)){
			String message = messageSource.getMessage("error." + fieldName + ".notEqual", new Object[]{ val0, val1 }, locale);

			fieldErrorList.add(new FieldError("", fieldName, message));
		}
	}
}
