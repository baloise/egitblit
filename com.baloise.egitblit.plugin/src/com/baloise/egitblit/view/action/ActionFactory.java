package com.baloise.egitblit.view.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

/**
 * Class for managing actions, like mapping to commands
 * 
 * @author MicBag
 *
 */
public class ActionFactory{

	List<Action> actionList = new ArrayList<Action>();
	IHandlerService handlerService;
	
	public ActionFactory(ViewPart part){
		if(part == null){
			throw new IllegalArgumentException("Missing argument ViewPart. Can't register actions.");
		}
		handlerService = (IHandlerService)part.getSite().getService(IHandlerService.class);
	}

	public void addAction(Action action){
		if(action == null){
			throw new IllegalArgumentException("Missing argument action.");
		}
		actionList.add(action);
		handlerService.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
	}
	
	public Action getAction(String id){
		if(id == null || id.trim().isEmpty()){
			return null;
		}
		for(Action action : this.actionList){
			if(id.equals(action.getActionDefinitionId())){
				action.setEnabled(action.isEnabled()); // Thank´s eclipse. State will not be updated in menu
				return action;
			}
		}
		return null;
	}
}
