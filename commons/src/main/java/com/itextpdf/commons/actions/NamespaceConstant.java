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
package com.itextpdf.commons.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class NamespaceConstant {
    
    public static final String ITEXT = "com.itextpdf";

    //Core
    public static final String CORE_EVENTS = ITEXT + ".events";
    public static final String CORE_IO = ITEXT + ".io";
    public static final String CORE_KERNEL = ITEXT + ".kernel";
    public static final String CORE_LAYOUT = ITEXT + ".layout";
    public static final String CORE_BARCODES = ITEXT + ".barcodes";
    public static final String CORE_PDFA = ITEXT + ".pdfa";
    public static final String CORE_SIGN = ITEXT + ".signatures";
    public static final String CORE_FORMS = ITEXT + ".forms";
    public static final String CORE_SXP = ITEXT + ".styledxmlparser";
    public static final String CORE_SVG = ITEXT + ".svg";
    
    //Addons
    public static final String PDF_HTML = ITEXT + ".html2pdf";
    public static final String PDF_SWEEP = ITEXT + ".pdfcleanup";
    public static final String PDF_OCR = ITEXT + ".pdfocr";
    public static final String PDF_OCR_TESSERACT4 = PDF_OCR + ".tesseract4";

    public static final List<String> ITEXT_CORE_NAMESPACES = Collections.unmodifiableList(Arrays.asList(
            NamespaceConstant.CORE_EVENTS,
            NamespaceConstant.CORE_IO,
            NamespaceConstant.CORE_KERNEL,
            NamespaceConstant.CORE_LAYOUT,
            NamespaceConstant.CORE_BARCODES,
            NamespaceConstant.CORE_PDFA,
            NamespaceConstant.CORE_SIGN,
            NamespaceConstant.CORE_FORMS,
            NamespaceConstant.CORE_SXP,
            NamespaceConstant.CORE_SVG)
    );

    private NamespaceConstant() {
    }
}
