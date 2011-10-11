package org.deegree.feature.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.gml.schema.GMLSchemaInfoSet;

/**
 * Defines a number of {@link FeatureType}s and their derivation hierarchy.
 * <p>
 * Some notes:
 * <ul>
 * <li>May be based on a {@link GMLSchemaInfoSet}. If it is build from a GML schema, use {@link #getGMLSchema()} to
 * access to the full XML schema infoset.</li>
 * <li>There is no default head for the feature type substitution relation as in GML (prior to GML 3.2: element
 * <code>gml:_Feature</code>, since 3.2: <code>gml:AbstractFeature</code>). This is not necessary, as each
 * {@link FeatureType} object is already identified being a feature type by its class.</li>
 * </ul>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface AppSchema {

    /**
     * Returns all feature types that are defined in this application schema.
     * 
     * @return all feature types, never <code>null</code>
     */
    FeatureType[] getFeatureTypes();

    /**
     * Returns all feature types that are defined in this application schema, limited by the options.
     * 
     * @param namespace
     *            may be <code>null</code> (include all feature types from all namespaces)
     * @param includeCollections
     *            set to <code>true</code>, if feature collection types shall be included, <code>false</code> otherwise
     * @param includeAbstracts
     *            set to <code>true</code>, if abstract types shall be included, <code>false</code> otherwise
     * 
     * @return all feature types, never <code>null</code>
     */
    List<FeatureType> getFeatureTypes( String namespace, boolean includeCollections, boolean includeAbstracts );

    /**
     * Returns all root feature types that are defined in this application schema.
     * 
     * @return all root feature types, never <code>null</code>
     */
    FeatureType[] getRootFeatureTypes();

    /**
     * Retrieves the feature type with the given name.
     * 
     * @param ftName
     *            feature type name to look up, must not be <code>null</code>
     * @return the feature type with the given name, or <code>null</code> if no such feature type exists
     */
    FeatureType getFeatureType( QName ftName );

    /**
     * Retrieves the direct subtypes for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return the direct subtypes of the given feature type (abstract and non-abstract)
     */
    FeatureType[] getDirectSubtypes( FeatureType ft );

    /**
     * Retrieves the parent feature type for the specified feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return parent feature type, can be <code>null</code>
     */
    FeatureType getParent( FeatureType ft );

    /**
     * Retrieves all substitutions (abstract and non-abstract ones) for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return all substitutions for the given feature type, never <code>null</code>
     */
    FeatureType[] getSubtypes( FeatureType ft );

    /**
     * Retrieves all concrete substitutions for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return all concrete substitutions for the given feature type, never <code>null</code>
     */
    FeatureType[] getConcreteSubtypes( FeatureType ft );

    /**
     * Returns the underlying {@link GMLSchemaInfoSet}
     * 
     * @return the underlying GML schema, can be <code>null</code> (not based on a GML schema)
     */
    GMLSchemaInfoSet getGMLSchema();

    /**
     * Determines whether a feature type is substitutable for another feature type.
     * <p>
     * This is true, iff <code>substitution</code> is either:
     * <ul>
     * <li>equal to <code>ft</code></li>
     * <li>a direct subtype of <code>ft</code></li>
     * <li>a transititive subtype of <code>ft</code></li>
     * </ul>
     * 
     * @param ft
     *            base feature type, must be part of this schema
     * @param substitution
     *            feature type to be checked, must be part of this schema
     * @return <code>true</code>, if the second feature type is a valid substitution for the first one
     */
    boolean isSubType( FeatureType ft, FeatureType substitution );

    /**
     * Returns the {@link PropertyType}s from the specified {@link FeatureType} declaration that are *not* present in
     * the parent {@link FeatureType} or its ancestors.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return list of property declarations, may be empty, but never <code>null</code>
     */
    List<PropertyType> getNewPropertyDecls( FeatureType ft );

    Map<FeatureType, FeatureType> getFtToSuperFt();

    /**
     * Returns the preferred namespace bindings for all namespaces.
     * 
     * @return the preferred namespace bindings for all namespaces, never <code>null</code>
     */
    Map<String, String> getNamespaceBindings();

    /**
     * Returns the child elements that the given complex type definition allows for.
     * <p>
     * TODO: Respect order and cardinality of child elements.
     * </p>
     * 
     * @param type
     *            complex type definition, must not be <code>null</code>
     * @return the child elements, never <code>null</code>
     */
    Map<QName, XSElementDeclaration> getAllowedChildElementDecls( XSComplexTypeDefinition type );

    /**
     * Returns the application namespaces.
     * <p>
     * NOTE: This excludes the GML core namespaces.
     * </p>
     * 
     * @return the application namespaces, never <code>null</code>
     */
    Set<String> getAppNamespaces();

    /**
     * Returns the namespaces that the definitions in the given namespace depend upon (excluding transitive
     * dependencies).
     * 
     * @param ns
     *            application namespace, must not be <code>null</code>
     * @return namespace dependencies, may be empty, but never <code>null</code>
     */
    List<String> getNamespacesDependencies( String ns );

    /**
     * Returns the {@link ObjectPropertyType} for the given element declaration (if it defines an object property).
     * 
     * @param elDecl
     *            element declaration, must not be <code>null</code>
     * @return property declaration or <code>null</code> (if the element does not declare an {@link ObjectPropertyType})
     */
    ObjectPropertyType getCustomElDecl( XSElementDeclaration elDecl );

}