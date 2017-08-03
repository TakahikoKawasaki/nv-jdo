/*
 * Copyright (C) 2017 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.neovisionaries.jdo;


/**
 * Query language.
 *
 * @since 1.17
 *
 * @author Takahiko Kawasaki
 */
public enum QueryLanguage
{
    /**
     * JDOQL; The identifier is "{@code javax.jdo.query.JDOQL}".
     */
    JDOQL("javax.jdo.query.JDOQL"),


    /**
     * SQL; The identifier is "{@code javax.jdo.query.SQL}".
     */
    SQL("javax.jdo.query.SQL"),
    ;


    /**
     * The identifier of this query language.
     */
    private final String identifier;


    /**
     * Private constructor with the identifier of this query language.
     *
     * @param identifier
     *         The identifier of this query language.
     */
    private QueryLanguage(String identifier)
    {
        this.identifier = identifier;
    }


    /**
     * Get the identifier of this query language. For example,
     * {@link #JDOQL}.{@code getIdentifier()} returns {@code
     * "javax.jdo.query.JDOQL"}.
     *
     * @return
     *         The identifier of this query language.
     */
    public String getIdentifier()
    {
        return identifier;
    }
}
