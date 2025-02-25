package ru.aviasales.admin.configuration;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(in = ParameterIn.QUERY,
        description = "Номер страницы",
        name = "page",
        schema = @Schema(type = "integer", defaultValue = "0"))
@Parameter(in = ParameterIn.QUERY,
        description = "Размер страницы",
        name = "size",
        schema = @Schema(type = "integer", defaultValue = "10"))
@Parameter(in = ParameterIn.QUERY,
        description = "Критерий сортировки в формате: property(,asc|desc).",
        name = "sort",
        array = @ArraySchema(schema = @Schema(type = "string")))
public @interface PageableAsQueryParam {
}
