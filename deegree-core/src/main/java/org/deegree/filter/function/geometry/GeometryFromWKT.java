//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.filter.function.geometry;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.cs.CRS;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.geometry.io.WKTReader;
import org.slf4j.Logger;

import com.vividsolutions.jts.io.ParseException;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryFromWKT implements FunctionProvider {

    static final Logger LOG = getLogger( GeometryFromWKT.class );

    private static final String NAME = "GeometryFromWKT";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getArgCount() {
        return 2;
    }

    @Override
    public Function create( List<Expression> params ) {
        return new Function( NAME, params ) {

            @Override
            public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                                    throws FilterEvaluationException {
                TypedObjectNode[] crs = getParams()[0].evaluate( obj, xpathEvaluator );
                TypedObjectNode[] geom = getParams()[1].evaluate( obj, xpathEvaluator );
                if ( crs.length != 1 ) {
                    throw new FilterEvaluationException( "The GeometryFromWKT function's first argument must "
                                                         + "evaluate to exactly one value." );
                }
                if ( geom.length != 1 ) {
                    throw new FilterEvaluationException( "The GeometryFromWKT function's second argument must "
                                                         + "evaluate to exactly one value." );
                }
                CRS srs = new CRS( crs[0].toString() );
                String wkt = geom[0].toString();
                WKTReader reader = new WKTReader( srs );
                try {
                    return new TypedObjectNode[] { reader.read( wkt ) };
                } catch ( ParseException e ) {
                    LOG.trace( "Stack trace:", e );
                    throw new FilterEvaluationException( "GeometryFromWKT error while parsing WKT: "
                                                         + e.getLocalizedMessage() );
                }
            }
        };
    }

}
