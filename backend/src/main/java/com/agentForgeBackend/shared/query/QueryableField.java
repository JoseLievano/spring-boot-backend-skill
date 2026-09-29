package com.agentForgeBackend.shared.query;

import com.agentForgeBackend.exceptions.InvalidQueryRequestException;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.EnumExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class QueryableField<ENTITY, VALUE> {

    private static final Set<FilterOperator> STRING_OPERATORS = Set.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.CONTAINS,
            FilterOperator.STARTS_WITH,
            FilterOperator.ENDS_WITH,
            FilterOperator.IN);

    private static final Set<FilterOperator> NUMBER_OPERATORS = Set.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.GREATER_THAN,
            FilterOperator.GREATER_THAN_OR_EQUAL,
            FilterOperator.LESS_THAN,
            FilterOperator.LESS_THAN_OR_EQUAL,
            FilterOperator.IN);

    private static final Set<FilterOperator> BOOLEAN_OPERATORS = Set.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS);

    private static final Set<FilterOperator> ENUM_OPERATORS = Set.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.IN);

    private static final Set<FilterOperator> DATE_TIME_OPERATORS = Set.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.GREATER_THAN,
            FilterOperator.GREATER_THAN_OR_EQUAL,
            FilterOperator.LESS_THAN,
            FilterOperator.LESS_THAN_OR_EQUAL);

    private final String apiName;
    private final Class<VALUE> valueType;
    private final Expression<?> expression;
    private final Set<FilterOperator> supportedOperators;
    private final String sortProperty;
    private final RawValueConverter<VALUE> converter;
    private final FieldPredicateFactory predicateFactory;
    private final NullPredicateFactory nullPredicateFactory;

    private QueryableField(
            String apiName,
            Class<VALUE> valueType,
            Expression<?> expression,
            Set<FilterOperator> supportedOperators,
            String sortProperty,
            RawValueConverter<VALUE> converter,
            FieldPredicateFactory predicateFactory,
            NullPredicateFactory nullPredicateFactory) {

        if (apiName == null || apiName.isBlank()) {
            throw new IllegalArgumentException("Queryable field API name must not be blank.");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("Queryable field value type must not be null.");
        }
        if (expression == null) {
            throw new IllegalArgumentException("Queryable field expression must not be null.");
        }
        if (supportedOperators == null || supportedOperators.isEmpty()) {
            throw new IllegalArgumentException("Queryable field must support at least one operator.");
        }
        if (sortProperty != null && sortProperty.isBlank()) {
            throw new IllegalArgumentException("Queryable field sort property must not be blank.");
        }

        this.apiName = apiName;
        this.valueType = valueType;
        this.expression = expression;
        this.supportedOperators = Set.copyOf(supportedOperators);
        this.sortProperty = sortProperty;
        this.converter = converter;
        this.predicateFactory = predicateFactory;
        this.nullPredicateFactory = nullPredicateFactory;
    }

    public static <ENTITY> QueryableField<ENTITY, String> string(
            String apiName,
            StringExpression expression) {

        return new QueryableField<>(
                apiName,
                String.class,
                expression,
                STRING_OPERATORS,
                null,
                QueryableField::convertString,
                (operator, rawValue) -> buildStringPredicate(apiName, expression, operator, rawValue),
                operator -> operator == FilterOperator.IS_NULL ? expression.isNull() : expression.isNotNull());
    }

    public static <ENTITY, VALUE extends Number & Comparable<?>> QueryableField<ENTITY, VALUE> number(
            String apiName,
            NumberExpression<VALUE> expression,
            Class<VALUE> valueType) {

        return new QueryableField<>(
                apiName,
                valueType,
                expression,
                NUMBER_OPERATORS,
                null,
                (fieldName, rawValue) -> convertNumber(fieldName, rawValue, valueType),
                (operator, rawValue) -> buildNumberPredicate(apiName, expression, valueType, operator, rawValue),
                operator -> operator == FilterOperator.IS_NULL ? expression.isNull() : expression.isNotNull());
    }

    public static <ENTITY> QueryableField<ENTITY, Boolean> booleanField(
            String apiName,
            BooleanExpression expression) {

        return new QueryableField<>(
                apiName,
                Boolean.class,
                expression,
                BOOLEAN_OPERATORS,
                null,
                QueryableField::convertBoolean,
                (operator, rawValue) -> buildBooleanPredicate(apiName, expression, operator, rawValue),
                operator -> operator == FilterOperator.IS_NULL ? expression.isNull() : expression.isNotNull());
    }

    public static <ENTITY, VALUE extends Enum<VALUE>> QueryableField<ENTITY, VALUE> enumField(
            String apiName,
            EnumExpression<VALUE> expression,
            Class<VALUE> valueType) {

        return new QueryableField<>(
                apiName,
                valueType,
                expression,
                ENUM_OPERATORS,
                null,
                (fieldName, rawValue) -> convertEnum(fieldName, rawValue, valueType),
                (operator, rawValue) -> buildEnumPredicate(apiName, expression, valueType, operator, rawValue),
                operator -> operator == FilterOperator.IS_NULL ? expression.isNull() : expression.isNotNull());
    }

    public static <ENTITY, VALUE extends Comparable> QueryableField<ENTITY, VALUE> dateTime(
            String apiName,
            DateTimeExpression<VALUE> expression,
            Class<VALUE> valueType) {

        return new QueryableField<>(
                apiName,
                valueType,
                expression,
                DATE_TIME_OPERATORS,
                null,
                (fieldName, rawValue) -> convertDateTime(fieldName, rawValue, valueType),
                (operator, rawValue) -> buildDateTimePredicate(apiName, expression, valueType, operator, rawValue),
                operator -> operator == FilterOperator.IS_NULL ? expression.isNull() : expression.isNotNull());
    }

    public static <ENTITY, VALUE extends Comparable> QueryableField<ENTITY, VALUE> date(
            String apiName,
            DateExpression<VALUE> expression,
            Class<VALUE> valueType) {

        return new QueryableField<>(
                apiName,
                valueType,
                expression,
                DATE_TIME_OPERATORS,
                null,
                (fieldName, rawValue) -> convertDateTime(fieldName, rawValue, valueType),
                (operator, rawValue) -> buildDatePredicate(apiName, expression, valueType, operator, rawValue),
                operator -> operator == FilterOperator.IS_NULL ? expression.isNull() : expression.isNotNull());
    }

    public String apiName() {
        return apiName;
    }

    public Class<VALUE> valueType() {
        return valueType;
    }

    public Expression<?> expression() {
        return expression;
    }

    public Set<FilterOperator> supportedOperators() {
        return supportedOperators;
    }

    public boolean supports(FilterOperator operator) {
        return supportedOperators.contains(operator);
    }

    public boolean isSortable() {
        return sortProperty != null;
    }

    public String sortProperty() {
        return sortProperty;
    }

    public QueryableField<ENTITY, VALUE> sortable(String sortProperty) {
        return new QueryableField<>(
                apiName,
                valueType,
                expression,
                supportedOperators,
                sortProperty,
                converter,
                predicateFactory,
                nullPredicateFactory);
    }

    public QueryableField<ENTITY, VALUE> nullable() {
        EnumSet<FilterOperator> operators = EnumSet.copyOf(supportedOperators);
        operators.add(FilterOperator.IS_NULL);
        operators.add(FilterOperator.IS_NOT_NULL);

        return new QueryableField<>(
                apiName,
                valueType,
                expression,
                operators,
                sortProperty,
                converter,
                predicateFactory,
                nullPredicateFactory);
    }

    public VALUE convertValue(Object rawValue) throws InvalidQueryRequestException {
        return converter.convert(apiName, rawValue);
    }

    public List<VALUE> convertValues(Object rawValue) throws InvalidQueryRequestException {
        return convertValues(apiName, rawValue, converter);
    }

    public BooleanExpression buildPredicate(FilterOperator operator, Object rawValue)
            throws InvalidQueryRequestException {

        if (operator == null) {
            throw new InvalidQueryRequestException("Filter operator is required for field '" + apiName + "'.");
        }
        if (!supports(operator)) {
            throw unsupportedOperator(apiName, operator);
        }
        if (operator == FilterOperator.IS_NULL || operator == FilterOperator.IS_NOT_NULL) {
            return nullPredicateFactory.build(operator);
        }

        return predicateFactory.build(operator, rawValue);
    }

    private static BooleanExpression buildStringPredicate(
            String apiName,
            StringExpression expression,
            FilterOperator operator,
            Object rawValue) throws InvalidQueryRequestException {

        return switch (operator) {
            case EQUALS -> expression.eq(convertString(apiName, rawValue));
            case NOT_EQUALS -> expression.ne(convertString(apiName, rawValue));
            case CONTAINS -> expression.containsIgnoreCase(convertString(apiName, rawValue));
            case STARTS_WITH -> expression.startsWithIgnoreCase(convertString(apiName, rawValue));
            case ENDS_WITH -> expression.endsWithIgnoreCase(convertString(apiName, rawValue));
            case IN -> expression.in(convertValues(apiName, rawValue, QueryableField::convertString));
            default -> throw unsupportedOperator(apiName, operator);
        };
    }

    private static <VALUE extends Number & Comparable<?>> BooleanExpression buildNumberPredicate(
            String apiName,
            NumberExpression<VALUE> expression,
            Class<VALUE> valueType,
            FilterOperator operator,
            Object rawValue) throws InvalidQueryRequestException {

        return switch (operator) {
            case EQUALS -> expression.eq(convertNumber(apiName, rawValue, valueType));
            case NOT_EQUALS -> expression.ne(convertNumber(apiName, rawValue, valueType));
            case GREATER_THAN -> expression.gt(convertNumber(apiName, rawValue, valueType));
            case GREATER_THAN_OR_EQUAL -> expression.goe(convertNumber(apiName, rawValue, valueType));
            case LESS_THAN -> expression.lt(convertNumber(apiName, rawValue, valueType));
            case LESS_THAN_OR_EQUAL -> expression.loe(convertNumber(apiName, rawValue, valueType));
            case IN -> expression.in(convertValues(
                    apiName,
                    rawValue,
                    (fieldName, value) -> convertNumber(fieldName, value, valueType)));
            default -> throw unsupportedOperator(apiName, operator);
        };
    }

    private static BooleanExpression buildBooleanPredicate(
            String apiName,
            BooleanExpression expression,
            FilterOperator operator,
            Object rawValue) throws InvalidQueryRequestException {

        return switch (operator) {
            case EQUALS -> expression.eq(convertBoolean(apiName, rawValue));
            case NOT_EQUALS -> expression.ne(convertBoolean(apiName, rawValue));
            default -> throw unsupportedOperator(apiName, operator);
        };
    }

    private static <VALUE extends Enum<VALUE>> BooleanExpression buildEnumPredicate(
            String apiName,
            EnumExpression<VALUE> expression,
            Class<VALUE> valueType,
            FilterOperator operator,
            Object rawValue) throws InvalidQueryRequestException {

        return switch (operator) {
            case EQUALS -> expression.eq(convertEnum(apiName, rawValue, valueType));
            case NOT_EQUALS -> expression.ne(convertEnum(apiName, rawValue, valueType));
            case IN -> expression.in(convertValues(
                    apiName,
                    rawValue,
                    (fieldName, value) -> convertEnum(fieldName, value, valueType)));
            default -> throw unsupportedOperator(apiName, operator);
        };
    }

    private static <VALUE extends Comparable> BooleanExpression buildDateTimePredicate(
            String apiName,
            DateTimeExpression<VALUE> expression,
            Class<VALUE> valueType,
            FilterOperator operator,
            Object rawValue) throws InvalidQueryRequestException {

        return switch (operator) {
            case EQUALS -> expression.eq(convertDateTime(apiName, rawValue, valueType));
            case NOT_EQUALS -> expression.ne(convertDateTime(apiName, rawValue, valueType));
            case GREATER_THAN -> expression.gt(convertDateTime(apiName, rawValue, valueType));
            case GREATER_THAN_OR_EQUAL -> expression.goe(convertDateTime(apiName, rawValue, valueType));
            case LESS_THAN -> expression.lt(convertDateTime(apiName, rawValue, valueType));
            case LESS_THAN_OR_EQUAL -> expression.loe(convertDateTime(apiName, rawValue, valueType));
            default -> throw unsupportedOperator(apiName, operator);
        };
    }

    private static <VALUE extends Comparable> BooleanExpression buildDatePredicate(
            String apiName,
            DateExpression<VALUE> expression,
            Class<VALUE> valueType,
            FilterOperator operator,
            Object rawValue) throws InvalidQueryRequestException {

        return switch (operator) {
            case EQUALS -> expression.eq(convertDateTime(apiName, rawValue, valueType));
            case NOT_EQUALS -> expression.ne(convertDateTime(apiName, rawValue, valueType));
            case GREATER_THAN -> expression.gt(convertDateTime(apiName, rawValue, valueType));
            case GREATER_THAN_OR_EQUAL -> expression.goe(convertDateTime(apiName, rawValue, valueType));
            case LESS_THAN -> expression.lt(convertDateTime(apiName, rawValue, valueType));
            case LESS_THAN_OR_EQUAL -> expression.loe(convertDateTime(apiName, rawValue, valueType));
            default -> throw unsupportedOperator(apiName, operator);
        };
    }

    private static String convertString(String apiName, Object rawValue) throws InvalidQueryRequestException {
        if (rawValue instanceof String value) {
            return value;
        }

        throw invalidValue(apiName, "a JSON string", rawValue);
    }

    private static Boolean convertBoolean(String apiName, Object rawValue) throws InvalidQueryRequestException {
        if (rawValue instanceof Boolean value) {
            return value;
        }

        throw invalidValue(apiName, "a JSON boolean", rawValue);
    }

    private static <VALUE extends Number> VALUE convertNumber(
            String apiName,
            Object rawValue,
            Class<VALUE> valueType) throws InvalidQueryRequestException {

        if (!(rawValue instanceof Number number)) {
            throw invalidValue(apiName, "a JSON number", rawValue);
        }

        BigDecimal decimal = toBigDecimal(apiName, number);

        try {
            if (Long.class.equals(valueType)) {
                return valueType.cast(decimal.toBigIntegerExact().longValueExact());
            }
            if (Integer.class.equals(valueType)) {
                return valueType.cast(decimal.toBigIntegerExact().intValueExact());
            }
            if (Short.class.equals(valueType)) {
                return valueType.cast(decimal.toBigIntegerExact().shortValueExact());
            }
            if (Byte.class.equals(valueType)) {
                return valueType.cast(decimal.toBigIntegerExact().byteValueExact());
            }
            if (Double.class.equals(valueType)) {
                return valueType.cast(decimal.doubleValue());
            }
            if (Float.class.equals(valueType)) {
                return valueType.cast(decimal.floatValue());
            }
            if (BigInteger.class.equals(valueType)) {
                return valueType.cast(decimal.toBigIntegerExact());
            }
            if (BigDecimal.class.equals(valueType)) {
                return valueType.cast(decimal);
            }
        } catch (ArithmeticException ex) {
            throw new InvalidQueryRequestException(
                    "Field '" + apiName + "' requires a " + valueType.getSimpleName() + " compatible numeric value.");
        }

        throw new IllegalStateException("Unsupported numeric query field type: " + valueType.getName());
    }

    private static <VALUE extends Enum<VALUE>> VALUE convertEnum(
            String apiName,
            Object rawValue,
            Class<VALUE> valueType) throws InvalidQueryRequestException {

        if (!(rawValue instanceof String value)) {
            throw invalidValue(apiName, "an enum name string", rawValue);
        }

        try {
            return Enum.valueOf(valueType, value);
        } catch (IllegalArgumentException ex) {
            throw new InvalidQueryRequestException(
                    "Field '" + apiName + "' requires one of the declared " + valueType.getSimpleName() + " enum names.");
        }
    }

    private static <VALUE> VALUE convertDateTime(
            String apiName,
            Object rawValue,
            Class<VALUE> valueType) throws InvalidQueryRequestException {

        if (valueType.isInstance(rawValue)) {
            return valueType.cast(rawValue);
        }
        if (!(rawValue instanceof String value) || value.isBlank()) {
            throw invalidValue(apiName, "an ISO-8601 date/time string", rawValue);
        }

        try {
            if (Date.class.equals(valueType)) {
                return valueType.cast(Date.from(parseInstant(value)));
            }
            if (Instant.class.equals(valueType)) {
                return valueType.cast(parseInstant(value));
            }
            if (LocalDate.class.equals(valueType)) {
                return valueType.cast(LocalDate.parse(value));
            }
            if (LocalDateTime.class.equals(valueType)) {
                return valueType.cast(LocalDateTime.parse(value));
            }
            if (OffsetDateTime.class.equals(valueType)) {
                return valueType.cast(OffsetDateTime.parse(value));
            }
            if (ZonedDateTime.class.equals(valueType)) {
                return valueType.cast(ZonedDateTime.parse(value));
            }
        } catch (DateTimeParseException ex) {
            throw new InvalidQueryRequestException(
                    "Field '" + apiName + "' requires a valid ISO-8601 date/time string.");
        }

        throw new IllegalStateException("Unsupported date/time query field type: " + valueType.getName());
    }

    private static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (DateTimeParseException ignoredAgain) {
                try {
                    return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
                } catch (DateTimeParseException ignoredThird) {
                    return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
                }
            }
        }
    }

    private static BigDecimal toBigDecimal(String apiName, Number number) throws InvalidQueryRequestException {
        if (number instanceof BigDecimal decimal) {
            return decimal;
        }
        if (number instanceof BigInteger integer) {
            return new BigDecimal(integer);
        }
        if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
            return BigDecimal.valueOf(number.longValue());
        }
        if (number instanceof Float || number instanceof Double) {
            double value = number.doubleValue();
            if (!Double.isFinite(value)) {
                throw invalidValue(apiName, "a finite JSON number", number);
            }
            return BigDecimal.valueOf(value);
        }

        return new BigDecimal(number.toString());
    }

    private static <VALUE> List<VALUE> convertValues(
            String apiName,
            Object rawValue,
            RawValueConverter<VALUE> converter) throws InvalidQueryRequestException {

        if (!(rawValue instanceof Collection<?> values)) {
            throw invalidValue(apiName, "a JSON array", rawValue);
        }
        if (values.isEmpty()) {
            throw new InvalidQueryRequestException("Field '" + apiName + "' requires a non-empty array for IN.");
        }

        List<VALUE> convertedValues = new ArrayList<>(values.size());
        for (Object value : values) {
            convertedValues.add(converter.convert(apiName, value));
        }

        return List.copyOf(convertedValues);
    }

    private static InvalidQueryRequestException invalidValue(
            String apiName,
            String expected,
            Object rawValue) {

        return new InvalidQueryRequestException(
                "Field '" + apiName + "' requires " + expected + "; received " + rawType(rawValue) + ".");
    }

    private static InvalidQueryRequestException unsupportedOperator(String apiName, FilterOperator operator) {
        return new InvalidQueryRequestException("Operator " + operator + " is not supported for field '" + apiName + "'.");
    }

    private static String rawType(Object rawValue) {
        return rawValue == null ? "null" : rawValue.getClass().getSimpleName();
    }

    @FunctionalInterface
    private interface RawValueConverter<VALUE> {
        VALUE convert(String apiName, Object rawValue) throws InvalidQueryRequestException;
    }

    @FunctionalInterface
    private interface FieldPredicateFactory {
        BooleanExpression build(FilterOperator operator, Object rawValue) throws InvalidQueryRequestException;
    }

    @FunctionalInterface
    private interface NullPredicateFactory {
        BooleanExpression build(FilterOperator operator);
    }
}
