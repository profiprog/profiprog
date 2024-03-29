package com.profiprog.gwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class Wizard extends SimpleDialog {

	public interface WizardStep extends IsWidget, HasValueChangeHandlers<Boolean> {
		boolean isValid();
	}

	public interface IsWizardStep {
		WizardStep asWizardStep();
	}

	private String titleBase;
	private int currentStep = -1;
	private IsWizardStep[] steps;
	private SimplePanel simplePanel;
	private String currentStepTitle;
	private Button prev;
	private Button next;
	private Button apply;
	private HandlerRegistration buttonEnablerRegistration;
	private ValueChangeHandler<Boolean> buttonEnabler = new ValueChangeHandler<Boolean>() {
		@Override
		public void onValueChange(ValueChangeEvent<Boolean> event) {
			boolean valid = event.getValue().booleanValue();
			boolean last = currentStep + 1 == steps.length;
			next.setEnabled(valid && !last);
			apply.setEnabled(valid && last);
		}
	};

	/**
	 * @param titleBase prefix for title of active step.
	 * 
	 * <b>Note:</b> titleBase can contains two of characters '#'
	 * where first one will be replaced by number of current step
	 * and second one will be replaced by count of all steps.
	 */
	public Wizard(String titleBase) {
		super(titleBase);
		this.titleBase = titleBase;
	}

	@Override
	protected void initContent(Panel panel) {
		simplePanel = (SimplePanel) panel;
	}

	@Override
	protected void cleanUp() {
		setStep(0);
	}

	@Override
	public void setTitle(String title) {
		String base = titleBase.replaceFirst("#", String.valueOf(currentStep + 1)).replaceFirst("#", String.valueOf(steps.length));
		super.setTitle(base + title);
	}

	public void setWizardSteps(IsWizardStep[] builderSteps) {
		steps = builderSteps;
	}

	private void setStep(int step) {
		if (currentStep == step) return;
		if (buttonEnablerRegistration != null) buttonEnablerRegistration.removeHandler();

		if (currentStep != -1) steps[currentStep].asWizardStep().asWidget().setTitle(currentStepTitle);
		Widget widget = steps[currentStep = step].asWizardStep().asWidget();
		simplePanel.setWidget(widget);
		
		prev.setEnabled(currentStep > 0);
		next.setEnabled(currentStep + 1 < steps.length && steps[currentStep].asWizardStep().isValid());
		apply.setEnabled(currentStep + 1 == steps.length && steps[currentStep].asWizardStep().isValid());
		buttonEnablerRegistration = steps[currentStep].asWizardStep().addValueChangeHandler(buttonEnabler);
		
		setTitle(currentStepTitle = widget.getTitle());
		widget.setTitle(null);
	}

	@Override
	protected void initActionButton() {
		addButton("Cancel", true);

		prev = addButton("<< Back", false);
		prev.setEnabled(false);
		prev.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setStep(currentStep - 1);
			}
		});

		next = addButton("Next >>", false);
		next.setEnabled(false);
		next.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setStep(currentStep + 1);
			}
		});

		apply = addButton("Apply", true);
		apply.setEnabled(false);
		apply.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				apply();
			}
		});

		cleanUp();
	}

	protected abstract void apply();

	@Override
	public void show() {
		assert steps != null : "Did you forget ,setWizardSteps()?";
		super.show();
	}
}
