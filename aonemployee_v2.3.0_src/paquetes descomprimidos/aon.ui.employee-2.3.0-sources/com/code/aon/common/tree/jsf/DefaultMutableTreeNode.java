/*
 * Created on 04-sep-2006
 *
 */
package com.code.aon.common.tree.jsf;

import org.apache.myfaces.custom.tree2.TreeNodeBase;

import com.code.aon.common.tree.IConstants;
import com.code.aon.common.tree.ITreeNode;


/**
 * 
 * @author Consulting & Development. I�aki Ayerbe - 04-sep-2006
 * @since 1.0
 *
 */

public class DefaultMutableTreeNode extends TreeNodeBase implements ITreeNode /*IMutableTreeNode*/ {

    private Object userObject;

    /**
     * Indica el icono cuando la rama est� expandida.
     */
    private String iconOpen;

    /**
     * Indica el icono cuando la rama est� cerrada.
     */
    private String iconClose;

    /**
     * Referencia externa o hiperv�nculo.
     */
    private String reference;

    /**
     * Construye el nodo ra�z del �rbol.
     * 
     * @param description
     */
    public DefaultMutableTreeNode(String description) {
        super( IConstants.ROOT_TYPE, description, "0", false );
        this.userObject = description;
        this.iconOpen = IConstants.ROOT_ICON;
        this.iconClose = IConstants.ROOT_ICON;
        this.reference = IConstants.ROOT_NAV;
    }

    /**
     * Construye un nodo del �rbol.
     * 
     * @param userObject
     * @param type
     * @param description
     * @param identifier
     * @param leaf
     * @param icon
     * @param reference
     */
    public DefaultMutableTreeNode( Object userObject 
                                 , String type, String description, String identifier, boolean leaf 
                                 , String icon, String reference ) {
        super(type, description, identifier, leaf);
        this.userObject = userObject;
        this.iconOpen = icon;
        this.iconClose = icon;
        this.reference = reference;
    }

    /**
     * Construye un nodo del �rbol.
     * 
     * @param userObject
     * @param type
     * @param description
     * @param identifier
     * @param leaf
     * @param iOpen
     * @param iClose
     * @param reference
     */
    public DefaultMutableTreeNode( Object userObject 
                                 , String type, String description, String identifier, boolean leaf 
                                 , String iOpen, String iClose, String reference ) {
        super(type, description, identifier, leaf);
        this.userObject = userObject;
        this.iconOpen = iOpen;
        this.iconClose = iClose;
        this.reference = reference;
    }


    /* (non-Javadoc)
     * @see com.code.aon.common.tree.jsf.ITreeNode#getUserObject()
     */
    public Object getUserObject() {
        return this.userObject;
    }

    /* (non-Javadoc)
     * @see com.code.aon.common.tree.jsf.ITreeNode#getIconOpen()
     */
    public String getIconOpen() {
        return this.iconOpen;
    }

    /* (non-Javadoc)
     * @see com.code.aon.common.tree.jsf.ITreeNode#getIconClose()
     */
    public String getIconClose() {
        return this.iconClose;
    }

    /* (non-Javadoc)
     * @see com.code.aon.common.tree.jsf.ITreeNode#getReference()
     */
    public String getReference() {
        return this.reference;
    }

    /**
     * @param iconOpen The iconOpen to set.
     */
    public void setIconOpen(String iconOpen) {
        this.iconOpen = iconOpen;
    }

    /**
     * @param iconClose The iconClose to set.
     */
    public void setIconClose(String iconClose) {
        this.iconClose = iconClose;
    }

    /**
     * Asigna la acci�n a ajecutar por esta opci�n.
     * 
     * @param reference, una representaci�n de la acci�n a ajecutar por esta opci�n.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @param userObject The userObject to set.
     */
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

}
