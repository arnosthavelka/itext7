/*

    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.kernel.geom;

public class PageSize extends Rectangle implements Cloneable {

    public static final PageSize A0 = new PageSize(2384, 3370);
    public static final PageSize A1 = new PageSize(1684, 2384);
    public static final PageSize A2 = new PageSize(1190, 1684);
    public static final PageSize A3 = new PageSize(842, 1190);
    public static final PageSize A4 = new PageSize(595, 842);
    public static final PageSize A5 = new PageSize(420, 595);
    public static final PageSize A6 = new PageSize(298, 420);
    public static final PageSize A7 = new PageSize(210, 298);
    public static final PageSize A8 = new PageSize(148, 210);
    public static final PageSize A9 = new PageSize(105, 547);
    public static final PageSize A10 = new PageSize(74, 105);

    public static final PageSize B0 = new PageSize(2834, 4008);
    public static final PageSize B1 = new PageSize(2004, 2834);
    public static final PageSize B2 = new PageSize(1417, 2004);
    public static final PageSize B3 = new PageSize(1000, 1417);
    public static final PageSize B4 = new PageSize(708, 1000);
    public static final PageSize B5 = new PageSize(498, 708);
    public static final PageSize B6 = new PageSize(354, 498);
    public static final PageSize B7 = new PageSize(249, 354);
    public static final PageSize B8 = new PageSize(175, 249);
    public static final PageSize B9 = new PageSize(124, 175);
    public static final PageSize B10 = new PageSize(88, 124);

    public static final PageSize DEFAULT = A4;

    public static final PageSize EXECUTIVE = new PageSize(522, 756);
    public static final PageSize LEDGER = new PageSize(1224, 792);
    public static final PageSize LEGAL = new PageSize(612, 1008);
    public static final PageSize LETTER = new PageSize(612, 792);
    public static final PageSize TABLOID = new PageSize(792, 1224);

    public PageSize(float width, float height) {
        super(0, 0, width, height);
    }


    public PageSize(Rectangle box) {
        super(box.getX(), box.getY(), box.getWidth(), box.getHeight());
    }

    /**
     * Rotates {@link PageSize} clockwise.
     *
     * @return the rotated {@link PageSize}.
     */
    public PageSize rotate() {
        return new PageSize(height, width);
    }

    /**
     * Creates a "deep copy" of this PageSize, meaning the object returned by this method will be independent
     * of the object being cloned.
     * Note that although the return type of this method is {@link Rectangle},
     * the actual type of the returned object is {@link PageSize}.
     *
     * @return the copied PageSize.
     */
    @Override
    public Rectangle clone() {
        // super.clone is safe to return since all of the PagSize's fields are primitive.
        return super.clone();
    }
}
