/*
 *-------------------
 * The ASHReport.java is part of ASH Viewer
 *-------------------
 * 
 * ASH Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASH Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASH Viewer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2009, Alex Kardapolov, All rights reserved.
 *
 */
package org.ash.gui;

import com.sleepycat.je.DatabaseException;
import org.ash.database.ASHDatabase;
import org.ash.history.ASHDatabaseH;
import org.ash.util.ProgressBarUtil;
import org.jdesktop.swingx.JXTable;
import org.jfree.ui.NumberCellRenderer;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ASHrawdata extends JPanel {

	/** The main. */
	private JPanel main;

	/** The root. */
	private JFrame root;

	/** The database. */
	private ASHDatabaseH database;

	/** The date format. */
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Constructor.
	 *
	 * @param rootFrame0
	 * @param database0
	 */
	public ASHrawdata(JFrame rootFrame0, ASHDatabaseH database0, double begin, double end) {

		super();
		setLayout(new GridLayout(1, 1, 3, 3));

		this.database = database0;
		this.root = rootFrame0;

		this.main = new JPanel();
		this.main.setLayout(new BorderLayout());

		this.add(this.main);

		// Create empty panel
		JPanel ashReport = new JPanel();
		ashReport.setLayout(new GridLayout(1, 1, 3, 3));

		// The button panel
		JToolBar buttonPanel;
		buttonPanel = new JToolBar("PanelButton");
		buttonPanel.setFloatable(false);
		buttonPanel.setBorder(new EtchedBorder());

		// get ASH Report button
		JButton getASHReportButton = new JButton();
		getASHReportButton.setText("Get ASH raw data in table");
		getASHReportButton.setPreferredSize(new Dimension(100, 30));
		getASHReportButton.setActionCommand("ASHrawdata");

		ButtonPlanActionListener buttonListener = new ButtonPlanActionListener(
				ashReport, getASHReportButton, database, begin, end);

		getASHReportButton.addActionListener(buttonListener);

		// Layout of buttons
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(getASHReportButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(new JLabel(dateFormat.format(begin)+
						" <<" + getPeriod(begin,end) + ">> "+
						dateFormat.format(end)
				)
		);

		// Add buttonPanel, main
		this.main.add(buttonPanel, BorderLayout.NORTH);
		this.main.add(ashReport, BorderLayout.CENTER);
	}

	private class ButtonPlanActionListener implements ActionListener {
		JPanel panelASHReport;
		JButton getASHReportButton;
		ASHDatabaseH database;
		double begin;
		double end;

		public ButtonPlanActionListener(final JPanel tabsSQLPlan, final JButton getASHReportButton,
										final ASHDatabaseH database, final double begin, final double end) {
			super();
			this.panelASHReport = tabsSQLPlan;
			this.getASHReportButton = getASHReportButton;
			this.database = database;
			this.begin = begin;
			this.end = end;
		}

		public void actionPerformed(final ActionEvent e) {
			/** Get action command */
			final String str = e.getActionCommand();

			if (str.equalsIgnoreCase("ASHrawdata")) {
				getASHReport();
			}
		}

		private void getASHReport() {

			//Disable getASHReportButton
			//getASHReportButton.setEnabled(false);

			// Clear tabbedpane
			JPanel panelLoading = createProgressBar("Loading, please wait...");
			panelASHReport.removeAll();
			panelASHReport.add(panelLoading);

			Thread t = new Thread() {
				@Override
				public void run() {
					// delay
					try {
						Thread.sleep(5L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					root.repaint();

						/*----------------------*/
					JXTable table;
					DefaultTableModel model;
					try {
						model = database.getASHRawData(begin, end);

						table = new JXTable(model);

						table.setColumnControlVisible(true);
						table.setHorizontalScrollEnabled(true);

						JPanel p = new JPanel(new BorderLayout());

						JScrollPane tableRawDataPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						tableRawDataPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

						tableRawDataPane.setViewportView(table);
						tableRawDataPane.setVerticalScrollBar(tableRawDataPane.getVerticalScrollBar());

						p.add(tableRawDataPane);
						p.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(
								"Selected Items: "), new EmptyBorder(4, 4, 4, 4)));

						/*----------------------*/

						panelASHReport.removeAll();
						panelASHReport.add(p);

						root.repaint();

					} catch (DatabaseException e) {
						e.printStackTrace();
					}


				}
			};
			t.start();
		}
	}

	/**
	 * Creates the progress bar.
	 *
	 * @param msg the msg
	 *
	 * @return the j panel
	 */
	private JPanel createProgressBar(String msg) {
		JProgressBar progress = ProgressBarUtil.createJProgressBar(msg);
		progress.setPreferredSize(new Dimension(250, 30));
		JPanel panel = new JPanel();
		panel.add(progress);
		return panel;
	}

	/**
	 * Get period in mm, dd, hh, ss
	 * @param begind
	 * @param endd
	 * @return
	 */
	private String getPeriod(double begind, double endd){
		String out = "";
		Double beginD = begind;
		Double endD = endd;
		DateTime start = new DateTime(beginD.longValue());
		DateTime end = new DateTime(endD.longValue());

		Period period = new Period(start, end);

		if (period.getMonths() > 0)
			out = period.getMonths()+" m. ";
		if (period.getDays() > 0)
			out = out + period.getDays()+" d. ";
		if (period.getHours() > 0)
			out = out + period.getHours()+" h. ";
		if (period.getMinutes() > 0)
			out = out + period.getMinutes()+" min. ";
		if (period.getSeconds() > 0)
			out = out + period.getSeconds()+" sec. ";

		return out;
	}

}
