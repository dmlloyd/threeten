/*
 * Copyright (c) 2007-2009, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.calendar.zone;

import java.io.Serializable;

import javax.time.InstantProvider;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.ZoneOffset;

/**
 * Implementation of zone rules for fixed offsets.
 *
 * @author Stephen Colebourne
 */
final class FixedZoneRules extends ZoneRules implements Serializable {

    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = 21787269186981L;
    /**
     * The fixed offset.
     */
    private final ZoneOffset offset;

    /**
     * Constructor.
     *
     * @param offset  the zone offset, not null
     */
    FixedZoneRules(ZoneOffset offset) {
        ZoneRules.checkNotNull(offset, "ZoneOffset must not be null");
        this.offset = offset;
    }

    /** {@inheritDoc} */
    @Override
    public ZoneOffset getOffset(InstantProvider instant) {
        return offset;
    }

    /** {@inheritDoc} */
    @Override
    public OffsetInfo getOffsetInfo(LocalDateTime dateTime) {
        return createOffsetInfo(dateTime, offset);
    }

    /** {@inheritDoc} */
    @Override
    public ZoneOffset getStandardOffset(InstantProvider instant) {
        return offset;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFixed() {
        return true;
    }

    //-----------------------------------------------------------------------
    /**
     * Is this instance equal to that specified by comparing the offset.
     *
     * @param otherRules  the other rules, null returns false
     * @return true if this rules is the same as that specified
     */
    @Override
    public boolean equals(Object otherRules) {
        if (this == otherRules) {
           return true;
        }
        if (otherRules instanceof FixedZoneRules) {
            return offset.equals(((FixedZoneRules) otherRules).offset);
        }
        return false;
    }

    /**
     * A hash code for the rules object.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return offset.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string representation of the rules using the ID.
     *
     * @return the rules ID, never null
     */
    @Override
    public String toString() {
        return offset == ZoneOffset.UTC ? "UTC" : "UTC" + offset.getID();
    }

}