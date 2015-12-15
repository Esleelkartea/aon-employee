/*
 * Created on 21-nov-2005
 *
 */
package com.code.aon.ui.planner;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import net.fortuna.ical4j.model.Recur;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane;

import com.code.aon.calendar.CalendarUtil;
import com.code.aon.calendar.enumeration.EventCategory;
import com.code.aon.planner.EventException;
import com.code.aon.planner.IEvent;
import com.code.aon.planner.IPlannerListener;
import com.code.aon.planner.IRecurrence;
import com.code.aon.planner.enumeration.EventStatus;
import com.code.aon.ui.planner.core.Event;
import com.code.aon.ui.planner.util.PlannerUtil;
import com.code.aon.ui.util.AonUtil;

/**
 * Event Manager.
 * 
 * @author Consulting & Development. I�aki Ayerbe - 23-nov-2005
 * @since 1.0
 *
 */
public class EventManager {

	public static final String MANAGER_BEAN_NAME = "event";
	private static final Logger LOGGER = Logger.getLogger(EventManager.class.getName());

	/** 
	 * Indica la lista de clases que implementa IPlannerListener 
	 * y que se cargan desde el faces-bean-config.xml. 
	 */
	private List<IPlannerListener> listenerClasses;
	/** Planner CallbackHandler */
	private IPlannerCallbackHandler cb;
	/** Indica la lista de objetos que escuchan al Controlador. */
	private List<IPlannerListener> listeners;
	/** Event manager Title. */
	private String title;
	/** Event */
	private IEvent event;
	/** Periodicidad. Envuelve a la clase <code>Recur</code>. */
	private Recurrence recurrence;
	/** Indica si se trata de un evento peri�dico. */
	private boolean recur = false;
	/** Indica el modo en el que se encuentra el Bean. Edici�n � Nuevo */
	private boolean isNew = true;
	/** Indica si est�n habilitadas las opciones de Aceptar y Borrar. */
	private boolean isEnabled = true;

	/** Fecha de inicio del evento. */
	private Date startDate;
	/** Hora de inicio del evento. */
	private Date startTime;
	/** Hora de finalizaci�n del evento. */
	private Date endTime;

	private Integer subcategory = 2;
	/**
	 * @return the subcategory
	 */
	public Integer getSubcategory() {
		return subcategory;
	}
	/**
	 * @param subcategory the subcategory to set
	 */
	public void setSubcategory(Integer subcategory) {
		this.subcategory = subcategory;
	}

	/**
	 * Bloquea las acciones a realizar sobre el evento en caso de tener eventos definidos internamente.
	 * Este caso se da en Eventos de la categor�a de WORK y FREEWORK.
	 */
	public void blockEventActions() {
		boolean hasEvents = 
			this.cb.hasEvents( this.event.getStartTime(), this.event.getEndTime(), null, this.event.getId() );
		if (hasEvents) {
			addMessage( FacesMessage.SEVERITY_WARN, "aon_hasevents_indate_error", new Date[] {this.event.getStartTime(), this.event.getEndTime()} );
		}
		setEnabled( this.event.getId() != null && !hasEvents );
	}

    /**
     * Return a list containing the listener classes.
     * 
     * @return List<IPlannerListener>
     */
    public List<IPlannerListener> getListenerClasses() {
        return listenerClasses;
    }

    /**
     * Set a list containing the listener classes.
     * 
     * @param listenerClasses
     */
    public void setListenerClasses(List<IPlannerListener> listenerClasses) {
        this.listenerClasses = listenerClasses;
        Iterator<IPlannerListener> iter = listenerClasses.iterator();
        while (iter.hasNext()) {
            IPlannerListener listener = iter.next();
            addPlannerListener(listener);
        }
    }

	/**
     * Add a IPlannerListener to the listener list
     * 
     * @param l - The IPlannerListener to be added
     */
    public void addPlannerListener(IPlannerListener l) {
        if (listeners == null) {
        	listeners = new LinkedList<IPlannerListener>();
        }
        if (!listeners.contains(l))
        	listeners.add(l);
    }

    /**
     * Remove a IPlannerListener from the listener list
     * 
     * @param l - The IPlannerListener to be added
     */
    public void removePlannerListener(IPlannerListener l) {
    	listeners.remove(l);
    }

    /**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
     * @return Returns the event.
     */
    public IEvent getEvent() {
        return event;
    }

    /**
     * @param event The event to set.
     */
    public void setEvent(IEvent event) {
        this.event = event;
    }

    /**
     * @return Returns the first recurrence.
     */
    public Recurrence getRecurrence() {
        return this.recurrence;
    }

    /**
     * @param event The first recurrence to set.
     */
    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    /**
	 * @return Returns the recur.
	 */
	public boolean isRecur() {
		return recur;
	}

	/**
	 * @param recur The recur to set.
	 */
	public void setRecur(boolean recur) {
		this.recur = recur;
	}

	/**
     * Comprueba si el evento esta en modo actualizaci�n o inserci�n.
     * 
     * @return
     */
    public boolean isNew() {
    	return isNew;
    }

    /**
	 * @param isNew The isNew to set.
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	/**
	 * Initialize Manager event object.
	 * 
	 * @param event
	 * @param cb
	 */
	public void initialize(IEvent event, IPlannerCallbackHandler cb, String title) {
		this.cb = cb;
		this.title = title;
		this.event = event;
		( (Event) this.event ).setSpreadable( this.cb.isSpreadable() );
//	TODO Por el momento solamente se trabaja con 1 RRULE.
		if ( event.getRecurrences().size() > 0 ) {
			this.recurrence = new Recurrence( event.getRecurrences().iterator().next() );
			this.setRecur(true);
		} else {
	    	this.recur = false;
	    	this.recurrence = null;
		}
		setEnabled( this.event.getId() != null );
		this.startDate = this.startTime = this.event.getStartTime();
		this.endTime = this.event.getEndTime();
	}

	/**
     * Returns a list of all status.
     * 
     * @return
     */
    public List<SelectItem> getEventStatus() {
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        LinkedList<SelectItem> types = new LinkedList<SelectItem>();
		for (EventStatus language : EventStatus.values()) {
			String name = language.getName(locale);
			SelectItem item = new SelectItem(language, name);
			types.add(item);
		}
        return types;
    }

    /**
     * Returns a list of all categories except WorkingTime.
     * 
     * @return
     */
    public List<SelectItem> getCategories() {
    	if (this.cb == null) {
	        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
	        LinkedList<SelectItem> types = new LinkedList<SelectItem>();
			for (EventCategory ec : EventCategory.values()) {
				String name = ec.getName(locale);
				if ( !ec.name().equals( EventCategory.WORK.name() ) ) {
					SelectItem item = new SelectItem(ec, name);
					types.add(item);
				}
			}
	        return types;
    	}
        return this.cb.getCategories();
    }

	/**
	 * This method gets called when the event setup folder fields has modified.
	 * 
	 * @param event
	 */
	public void setupChanged(ValueChangeEvent event) {
		String id = event.getComponent().getId();
		int index = id.indexOf( '_' );
		if ( index > -1 ) {
			String name = id.substring( index + 1, id.length() );
			try {
				PropertyUtils.setProperty( this.event, name, event.getNewValue() );
			} catch (IllegalAccessException e) {
				LOGGER.severe( e.getMessage() );
			} catch (InvocationTargetException e) {
				LOGGER.severe( e.getMessage() );
			} catch (NoSuchMethodException e) {
				LOGGER.severe( e.getMessage() );
			}
		}
	}

	/**
	 * This method gets called when the event start time has modified.
	 * 
	 * @param event
	 */
	public void startTimeChanged(ValueChangeEvent event) {
		this.startDate = this.startTime = (Date) event.getNewValue();
		if ( this.event.isAllDay() ) {
			PlannerUtil.setAllDayTimeEvent( (Event) this.event, this.startDate );
			this.startDate = this.startTime = this.event.getStartTime();
		}
    }

	/**
	 * This method gets called when the event end time has modified.
	 * 
	 * @param event
	 */
	public void endTimeChanged(ValueChangeEvent event) {
		this.event.getEndTime().setTime( ( (Date) event.getNewValue() ).getTime() );
		if ( this.startDate.after( this.event.getEndTime() ) ) {
			PlannerUtil.setCurrentTimeEvent( (Event) this.event, this.startDate );
		}
	}

    /**
     * This method gets called when an event duration has modified.
     * 
     * @param event
     */
	public void eventModeChanged(ValueChangeEvent event) {
		boolean bol = ( (Boolean)event.getNewValue() ).booleanValue();
		if ( bol || this.event.isHoliday() ) {
			PlannerUtil.setAllDayTimeEvent( (Event) this.event, this.event.getStartTime() );
		} else {
			PlannerUtil.setCurrentTimeEvent( (Event) this.event, new Date() );
		}
	}

    /**
     * Permite a�adir periodicidad al evento. Genera una periodicidad por defecto, diaria sin
     * fecha fin y habilita la modificaci�n de la misma.
     * 
     * @param event
     */
	public void allowRecurrence(ValueChangeEvent event) {
		this.recur = ( (Boolean)event.getNewValue() ).booleanValue();
		Recur r = new Recur(Recur.DAILY, CalendarUtil.getICalDate( this.event.getEndTime() ) );
		this.recurrence = new Recurrence(r);
		this.recurrence.setUntilType( IRecurrence.UNTILDATE );
		HtmlPanelTabbedPane tabbedPane = 
			(HtmlPanelTabbedPane)event.getComponent().getParent().getParent().getParent().getParent();
		if (tabbedPane != null) {
			if (this.recur)
				tabbedPane.setSelectedIndex(1);
			else
				tabbedPane.setSelectedIndex(0);
		}
	}

    /**
	 * @param isEnabled The isEnabled to set.
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

    /**
	 * @return Returns the isEnabled.
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
     * Save event.
     * 
     * @param evt the action event
     */
    public void accept(ActionEvent evt) throws ParseException {
        if ( this.cb != null ) {
        	Event _event = (Event) this.event;
        	fillEventTime( _event );
    		_event.setRecurrences( new HashSet<Recur>() );
        	if (this.recur) {
        		HashSet<Recur> set = new HashSet<Recur>();
                Recur _recur = new Recur(this.recurrence.getRRule());
        		set.add(_recur); 
        		_event.setRecurrences(set);
    			_event.setEndTime( this.recurrence.getEndDate( _event.getStartTime(), _event.getEndTime() ) );
        	}
    		_event.setRealStartTime( _event.getStartTime() );
    		_event.setRealEndTime( _event.getEndTime() );
			_event.setDirty(true);
        	try {
//	Primero mira a ver si hay eventos en esa fecha y despues comprueba si es un evento nuevo.
	        	if ( !this.cb.hasEvents( _event.getStartTime(), _event.getEndTime(), null, _event.getId() ) ) {
	        		if (isNew()) {
		        		this.cb.add( _event );
		        		isNew = false;
		        		fireEventAdded( _event );
					} else {
						this.cb.update( _event );
					    fireEventUpdated( _event );
					}
	        	} else {
	                addMessage( FacesMessage.SEVERITY_INFO, "aon_hasevents_indate_error", new Date[] { _event.getStartTime(), _event.getEndTime()} );
	        	}
//				if (isNew()) {
//		        	if ( !this.cb.hasEvents( _event.getStartTime(), _event.getEndTime(), null, _event.getId() ) ) {
//		        		this.cb.add(_event);
//		        		isNew = false;
//		        		fireEventAdded(_event);
//		        	} else {
//		                addMessage( FacesMessage.SEVERITY_INFO, "aon_hasevents_indate_error", new Date[] {_event.getStartTime(), _event.getEndTime()} );
//		        	}
//				} else {
//					this.cb.update(_event);
//				    fireEventUpdated(_event);
//				}
			} catch (EventException e) {
				String messageId = e.getMessage();
				Object[] obj = e.getParameters();
				if ( ControllerUtil.getPlannerBundle().getString( e.getMessage() ) == null )
					messageId = "aon_accept_event_exception";
				addMessage(FacesMessage.SEVERITY_WARN, messageId, obj);
			}
			onCancel(evt);
        } else {
            LOGGER.severe( "Planner Callback Handler is not defined." );
        }
    }

    /**
     * Elimina el evento en la agenda.
     * 
     * @param e the action event
     */
    public void remove(ActionEvent e) throws EventException, ParseException {
        if ( this.cb != null ) {
        	if ( !this.cb.hasEvents( this.event.getStartTime(), this.event.getEndTime(), null, this.event.getId() ) ) {
	           	this.cb.remove(this.event);
	            fireEventRemoved(this.event);
        	} else {
                addMessage( FacesMessage.SEVERITY_INFO, "aon_hasevents_indate_error", new Date[] {this.event.getStartTime(), this.event.getEndTime()} );
        	}
        } else {
            LOGGER.severe( "Planner Callback Handler is not defined." );            
        }
		onCancel(e);
    }

    /**
     * Cancela la edici�n del evento.
     * 
     * @return
     */
	public void onCancel(ActionEvent event) {
    	this.recur = false;
    	this.recurrence = null;
    	this.cb.refresh(null);
	}

    /**
     * Propaga un <code>EventAdded</code> a todos los subscriptores
     * registrados.
     * 
     * @param event El evento de esta acci�n.
     */
    protected void fireEventAdded(IEvent event) {
        Iterator<IPlannerListener> iter = getListeners().iterator();
        while (iter.hasNext()) {
            iter.next().eventAdded(event);
        }
    }

    /**
     * Propaga un <code>EventUpdated</code> a todos los subscriptores
     * registrados.
     * 
     * @param event El evento de esta acci�n.
     */
    protected void fireEventUpdated(IEvent event) {
        Iterator<IPlannerListener> iter = getListeners().iterator();
        while (iter.hasNext()) {
            iter.next().eventUpdated(event);;
        }
    }

    /**
     * Propaga un <code>EventRemoved</code> a todos los subscriptores
     * registrados.
     * 
     * @param event El evento de esta acci�n.
     */
    protected void fireEventRemoved(IEvent event) {
        Iterator<IPlannerListener> iter = getListeners().iterator();
        while (iter.hasNext()) {
            iter.next().eventRemoved(event);
        }
    }

    /**
     * Devuelve una copia de los subscriptores registrados.
     * 
     * @return List La copia de los subscriptores registrados. 
     */
    private List<IPlannerListener> getListeners() {
        List<IPlannerListener> tmpList = new LinkedList<IPlannerListener>();
        if (this.listeners != null) {
            tmpList.addAll( this.listeners );
        }
        return tmpList;
    }

	/**
	 * Fill Event time before saving it to the database.
	 */
	private void fillEventTime(Event _event) {
		if ( isRecur() ) {
			// Setting starting date and time
			Calendar startCalendarDate = Calendar.getInstance();
			startCalendarDate.setTime( this.startDate );
			Calendar startCalendarTime = Calendar.getInstance();
			startCalendarTime.setTime( this.startTime );
			startCalendarTime.set( Calendar.YEAR, startCalendarDate.get(Calendar.YEAR) );
			startCalendarTime.set( Calendar.MONDAY, startCalendarDate.get(Calendar.MONTH) );
			startCalendarTime.set( Calendar.DATE, startCalendarDate.get(Calendar.DATE) );
			_event.setStartTime( startCalendarTime.getTime() );
			// Setting ending date and time
			Calendar endCalendarDate = Calendar.getInstance();
			endCalendarDate.setTime( _event.getEndTime() );
			Calendar endCalendarTime = Calendar.getInstance();
			endCalendarTime.setTime( this.endTime );
			endCalendarTime.set( Calendar.YEAR, endCalendarDate.get(Calendar.YEAR) );
			endCalendarTime.set( Calendar.MONDAY, endCalendarDate.get(Calendar.MONTH) );
			endCalendarTime.set( Calendar.DATE, endCalendarDate.get(Calendar.DATE) );
			_event.setEndTime( endCalendarTime.getTime() );
		} else if ( this.event.isAllDay() ) {
			this.event.getStartTime().setTime( this.startDate.getTime() );
		}
	}

	/**
	 * @param severity
	 * @param messageId
	 * @param params
	 */
	private void addMessage(FacesMessage.Severity severity, String messageId, Object[] params) {
		FacesContext ctx = FacesContext.getCurrentInstance();
		String summary = ControllerUtil.getPlannerBundle().getString( messageId );
		FacesMessage msg = AonUtil.getMessage( ctx, summary, params );
		msg.setSeverity( severity );
		ctx.addMessage( AonUtil.AON_ERROR, msg );
	}

}
