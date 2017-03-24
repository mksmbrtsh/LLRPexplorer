/*
 *
 * This file was generated by LLRP Code Generator
 * see http://llrp-toolkit.cvs.sourceforge.net/llrp-toolkit/
 * for more information
 * Generated on: Sun Apr 08 14:14:12 EDT 2012;
 *
 */

/*
 * Copyright 2007 ETH Zurich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
package org.llrp.ltk.generated.parameters;

import maximsblog.blogspot.com.llrpexplorer.Logger;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.exceptions.MissingParameterException;
import org.llrp.ltk.generated.LLRPConstants;
import org.llrp.ltk.generated.interfaces.AirProtocolUHFRFModeTable;
import org.llrp.ltk.generated.parameters.C1G2UHFRFModeTableEntry;
import org.llrp.ltk.types.LLRPBitList;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.SignedShort;
import org.llrp.ltk.types.TLVParameter;
import org.llrp.ltk.types.TVParameter;
import org.llrp.ltk.types.UnsignedShort;

import java.util.LinkedList;
import java.util.List;


/**
 * This parameter carries the set of C1G2 RF modes that the Reader is capable of operating.

See also {@link <a href="http://www.epcglobalinc.org/standards/llrp/llrp_1_0_1-standard-20070813.pdf#page=98&view=fit">LLRP Specification Section 15.2.1.1.2</a>}
 and {@link <a href="http://www.epcglobalinc.org/standards/llrp/llrp_1_0_1-standard-20070813.pdf#page=153&view=fit">LLRP Specification Section 16.3.1.1.2</a>}


 */

/**
 * This parameter carries the set of C1G2 RF modes that the Reader is capable of operating.

See also {@link <a href="http://www.epcglobalinc.org/standards/llrp/llrp_1_0_1-standard-20070813.pdf#page=98&view=fit">LLRP Specification Section 15.2.1.1.2</a>}
 and {@link <a href="http://www.epcglobalinc.org/standards/llrp/llrp_1_0_1-standard-20070813.pdf#page=153&view=fit">LLRP Specification Section 16.3.1.1.2</a>}

      .
 */
public class C1G2UHFRFModeTable extends TLVParameter
    implements AirProtocolUHFRFModeTable {
    public static final SignedShort TYPENUM = new SignedShort(328);
    private static final Logger LOGGER = Logger.getLogger(C1G2UHFRFModeTable.class);
    protected List<C1G2UHFRFModeTableEntry> c1G2UHFRFModeTableEntryList = new LinkedList<C1G2UHFRFModeTableEntry>();

    /**
     * empty constructor to create new parameter.
     */
    public C1G2UHFRFModeTable() {
    }

    /**
     * Constructor to create parameter from binary encoded parameter
     * calls decodeBinary to decode parameter.
     * @param list to be decoded
     */
    public C1G2UHFRFModeTable(LLRPBitList list) {
        decodeBinary(list);
    }

    /**
    * Constructor to create parameter from xml encoded parameter
    * calls decodeXML to decode parameter.
    * @param element to be decoded
    */
    public C1G2UHFRFModeTable(Element element)
        throws InvalidLLRPMessageException {
        decodeXML(element);
    }

    /**
    * {@inheritDoc}
    */
    public LLRPBitList encodeBinarySpecific() {
        LLRPBitList resultBits = new LLRPBitList();

        if (c1G2UHFRFModeTableEntryList == null) {
            LOGGER.warn(" c1G2UHFRFModeTableEntryList not set");

            //parameter has to be set - throw exception
            throw new MissingParameterException(
                " c1G2UHFRFModeTableEntryList not set");
        } else {
            for (C1G2UHFRFModeTableEntry field : c1G2UHFRFModeTableEntryList) {
                resultBits.append(field.encodeBinary());
            }
        }

        return resultBits;
    }

    /**
    * {@inheritDoc}
    */
    public Content encodeXML(String name, Namespace ns) {
        // element in namespace defined by parent element
        Element element = new Element(name, ns);
        // child element are always in default LLRP namespace
        ns = Namespace.getNamespace("llrp", LLRPConstants.LLRPNAMESPACE);

        //parameters
        if (c1G2UHFRFModeTableEntryList == null) {
            LOGGER.warn(" c1G2UHFRFModeTableEntryList not set");
            throw new MissingParameterException(
                "  c1G2UHFRFModeTableEntryList not set");
        }

        for (C1G2UHFRFModeTableEntry field : c1G2UHFRFModeTableEntryList) {
            element.addContent(field.encodeXML(field.getClass().getName()
                                                    .replaceAll(field.getClass()
                                                                     .getPackage()
                                                                     .getName() +
                        ".", ""), ns));
        }

        return element;
    }

    /**
    * {@inheritDoc}
    */
    protected void decodeBinarySpecific(LLRPBitList binary) {
        int position = 0;
        int tempByteLength;
        int tempLength = 0;
        int count;
        SignedShort type;
        int fieldCount;
        Custom custom;

        // list of parameters
        c1G2UHFRFModeTableEntryList = new LinkedList<C1G2UHFRFModeTableEntry>();
        LOGGER.debug("decoding parameter c1G2UHFRFModeTableEntryList ");

        while (position < binary.length()) {
            // store if one parameter matched
            boolean atLeastOnce = false;

            // look ahead to see type
            if (binary.get(position)) {
                // do not take the first bit as it is always 1
                type = new SignedShort(binary.subList(position + 1, 7));
            } else {
                type = new SignedShort(binary.subList(position +
                            RESERVEDLENGTH, TYPENUMBERLENGTH));
                tempByteLength = new UnsignedShort(binary.subList(position +
                            RESERVEDLENGTH + TYPENUMBERLENGTH,
                            UnsignedShort.length())).toShort();
                tempLength = 8 * tempByteLength;
            }

            //add parameter to list if type number matches
            if ((type != null) && type.equals(C1G2UHFRFModeTableEntry.TYPENUM)) {
                if (binary.get(position)) {
                    // length can statically be determined for TV Parameters
                    tempLength = C1G2UHFRFModeTableEntry.length();
                }

                c1G2UHFRFModeTableEntryList.add(new C1G2UHFRFModeTableEntry(
                        binary.subList(position, tempLength)));
                LOGGER.debug(
                    "adding C1G2UHFRFModeTableEntry to c1G2UHFRFModeTableEntryList ");
                atLeastOnce = true;
                position += tempLength;
            }

            if (!atLeastOnce) {
                //no parameter matched therefore we jump out of the loop
                break;
            }
        }

        //if list is still empty no parameter matched
        if (c1G2UHFRFModeTableEntryList.isEmpty()) {
            LOGGER.warn(
                "encoded message does not contain parameter for non optional c1G2UHFRFModeTableEntryList");
            throw new MissingParameterException(
                "C1G2UHFRFModeTable misses non optional parameter of type C1G2UHFRFModeTableEntry");
        }
    }

    /**
    * {@inheritDoc}
    */
    public void decodeXML(Element element) throws InvalidLLRPMessageException {
        List<Element> tempList = null;
        boolean atLeastOnce = false;
        Custom custom;

        Element temp = null;

        // child element are always in default LLRP namespace
        Namespace ns = Namespace.getNamespace(LLRPConstants.LLRPNAMESPACE);

        //parameter - not choices - no special actions needed
        //we expect a list of parameters
        c1G2UHFRFModeTableEntryList = new LinkedList<C1G2UHFRFModeTableEntry>();
        tempList = element.getChildren("C1G2UHFRFModeTableEntry", ns);

        if ((tempList == null) || tempList.isEmpty()) {
            LOGGER.warn(
                "C1G2UHFRFModeTable misses non optional parameter of type c1G2UHFRFModeTableEntryList");
            throw new MissingParameterException(
                "C1G2UHFRFModeTable misses non optional parameter of type c1G2UHFRFModeTableEntryList");
        } else {
            for (Element e : tempList) {
                c1G2UHFRFModeTableEntryList.add(new C1G2UHFRFModeTableEntry(e));
                LOGGER.debug(
                    "adding C1G2UHFRFModeTableEntry to c1G2UHFRFModeTableEntryList ");
            }
        }

        element.removeChildren("C1G2UHFRFModeTableEntry", ns);

        if (element.getChildren().size() > 0) {
            String message = "C1G2UHFRFModeTable has unknown element " +
                ((Element) element.getChildren().get(0)).getName();
            throw new InvalidLLRPMessageException(message);
        }
    }

    //setters

    /**
    * set c1G2UHFRFModeTableEntryList of type  List &lt;C1G2UHFRFModeTableEntry>.
    * @param  c1G2UHFRFModeTableEntryList to be set
    */
    public void setC1G2UHFRFModeTableEntryList(
        final List<C1G2UHFRFModeTableEntry> c1G2UHFRFModeTableEntryList) {
        this.c1G2UHFRFModeTableEntryList = c1G2UHFRFModeTableEntryList;
    }

    // end setter

    //getters

    /**
    * get c1G2UHFRFModeTableEntryList of type List &lt;C1G2UHFRFModeTableEntry> .
    * @return  List &lt;C1G2UHFRFModeTableEntry>
    */
    public List<C1G2UHFRFModeTableEntry> getC1G2UHFRFModeTableEntryList() {
        return c1G2UHFRFModeTableEntryList;
    }

    // end getters

    //add methods

    /**
    * add element c1G2UHFRFModeTableEntry of type C1G2UHFRFModeTableEntry .
    * @param  c1G2UHFRFModeTableEntry of type C1G2UHFRFModeTableEntry
    */
    public void addToC1G2UHFRFModeTableEntryList(
        C1G2UHFRFModeTableEntry c1G2UHFRFModeTableEntry) {
        if (this.c1G2UHFRFModeTableEntryList == null) {
            this.c1G2UHFRFModeTableEntryList = new LinkedList<C1G2UHFRFModeTableEntry>();
        }

        this.c1G2UHFRFModeTableEntryList.add(c1G2UHFRFModeTableEntry);
    }

    // end add

    /**
    * For TLV Parameter length can not be determined at compile time. This method therefore always returns 0.
    * @return Integer always zero
    */
    public static Integer length() {
        return 0;
    }

    /**
    * {@inheritDoc}
    */
    public SignedShort getTypeNum() {
        return TYPENUM;
    }

    /**
    * {@inheritDoc}
    */
    public String getName() {
        return "C1G2UHFRFModeTable";
    }

    /**
    * return string representation. All field values but no parameters are included
    * @return String
    */
    public String toString() {
        String result = "C1G2UHFRFModeTable: ";
        result = result.replaceFirst(", ", "");

        return result;
    }
}
