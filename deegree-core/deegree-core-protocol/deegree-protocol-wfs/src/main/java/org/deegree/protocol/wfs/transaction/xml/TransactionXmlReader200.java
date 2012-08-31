//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.protocol.wfs.transaction.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValueAsQName;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.transaction.ReleaseAction;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionAction;
import org.deegree.protocol.wfs.transaction.action.Delete;

/**
 * Reader for XML encoded WFS 2.0.0 <code>Transaction</code> requests.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TransactionXmlReader200 extends AbstractWFSRequestXMLAdapter implements TransactionActionXmlReader {

    /**
     * Parses a WFS 2.0.0 <code>Transaction</code> document.
     * 
     * @return parsed {@link Transaction} request, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public Transaction read( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        xmlStream.require( START_ELEMENT, WFS_200_NS, "Transaction" );

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = getAttributeValue( xmlStream, "handle" );

        // <xsd:attribute name="lockId" type="xsd:string"/>
        String lockId = getAttributeValue( xmlStream, "lockId" );

        // <xsd:attribute name="releaseAction" type="wfs:AllSomeType" default="ALL"/>
        String releaseActionString = getAttributeValue( xmlStream, "releaseAction" );
        ReleaseAction releaseAction = parseReleaseAction( releaseActionString );

        // <xsd:attribute name="srsName" type="xsd:anyURI"/>
        String srsName = getAttributeValue( xmlStream, "srsName" );

        nextElement( xmlStream );
        LazyTransactionActionsReader iterable = new LazyTransactionActionsReader( xmlStream, this );
        return new Transaction( VERSION_200, handle, lockId, releaseAction, iterable );
    }

    private ReleaseAction parseReleaseAction( String releaseActionString ) {
        ReleaseAction releaseAction = null;
        if ( releaseActionString != null ) {
            if ( "SOME".equals( releaseActionString ) ) {
                releaseAction = ReleaseAction.SOME;
            } else if ( "ALL".equals( releaseActionString ) ) {
                releaseAction = ReleaseAction.ALL;
            } else {
                String msg = "Invalid value (=" + releaseActionString
                             + ") for release action parameter. Valid values are 'ALL' or 'SOME'.";
                throw new InvalidParameterValueException( msg, "releaseAction" );
            }
        }
        return releaseAction;
    }

    @Override
    public TransactionAction readAction( XMLStreamReader xmlStream )
                            throws XMLStreamException, XMLParsingException {
        if ( !WFS_200_NS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Unexpected element: " + xmlStream.getName()
                         + "' is not a WFS 2.0.0 transaction action element. Not in the WFS 2.0.0 namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        TransactionAction operation = null;
        String localName = xmlStream.getLocalName();
        if ( "Delete".equals( localName ) ) {
            operation = readDelete( xmlStream );
        } else if ( "Insert".equals( localName ) ) {
            operation = readInsert( xmlStream );
        } else if ( "Native".equals( localName ) ) {
            operation = readNative( xmlStream );
        } else if ( "Replace".equals( localName ) ) {
            operation = readReplace( xmlStream );
        } else if ( "Update".equals( localName ) ) {
            operation = readUpdate( xmlStream );
        } else {
            throw new XMLParsingException( xmlStream, "Unexpected operation element " + localName + "." );
        }
        nextElement( xmlStream );
        return operation;
    }

    /**
     * Returns the object representation of a <code>wfs:Delete</code> element. Consumes all corresponding events from
     * the given <code>XMLStream</code>.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Delete&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/wfs:Delete&gt;) afterwards
     * @return corresponding {@link Delete} object, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    Delete readDelete( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = xmlStream.getAttributeValue( null, "handle" );

        // <xsd:attribute name="typeName" type="xsd:QName" use="required"/>
        QName typeName = getRequiredAttributeValueAsQName( xmlStream, null, "typeName" );

        // required: 'fes:Filter'
        nextElement( xmlStream );
        try {
            xmlStream.require( START_ELEMENT, FES_20_NS, "Filter" );
        } catch ( XMLStreamException e ) {
            throw new MissingParameterException( "Mandatory 'fes:Filter' element is missing in Delete." );
        }
        Filter filter = Filter200XMLDecoder.parse( xmlStream );
        nextElement( xmlStream );
        xmlStream.require( END_ELEMENT, WFS_200_NS, "Delete" );
        return new Delete( handle, typeName, filter );
    }

    private TransactionAction readNative( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }

    private TransactionAction readReplace( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }

    private TransactionAction readUpdate( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }

    private TransactionAction readInsert( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }
}
