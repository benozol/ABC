/*
 * This file is part of the Automatic Brighton Classification Tool (ABC-Tool).
 *
 * The ABC-Tool is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The ABC-Tool is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero Affero General Public
 * License along with The ABC-Tool.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2008, 2012 Benedikt Becker
 */

package org.brightoncollaboration.abc;

/** Indicates an ABC configuration error or an error on beside of the client program.
 */
public class AbcConfigurationException extends Exception {

    private static final long serialVersionUID = -2961183405061267971L;

    public AbcConfigurationException() {
    }

    public AbcConfigurationException(String message) {
        super(message);
    }

    public AbcConfigurationException(Throwable cause) {
        super(cause);
    }

    public AbcConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
