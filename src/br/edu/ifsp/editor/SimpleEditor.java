package br.edu.ifsp.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import br.edu.ifsp.capture.Coordinate;
import br.edu.ifsp.capture.ImageCapture;
import br.edu.ifsp.capture.Segmentation;
import br.edu.ifsp.capture.ShowObject;
import br.edu.ifsp.util.CaptureData;
import br.edu.ifsp.util.Load;

public class SimpleEditor extends JFrame implements ChangeListener {

	private CaptureData data;
	private Load load;
	private ShowObject view;
	private Coordinate coor;
	private Segmentation seg;
	private ImageCapture imgDepth;
	private ImageCapture imgColor;

	private JSlider slider;

	public SimpleEditor() {
		this(null);
	}

	public SimpleEditor(File file) {
		super("Simple Editor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 580);
		setVisible(true);

		initialize(file);

		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(BorderLayout.CENTER, getComponent());
		c.add(BorderLayout.SOUTH, getControl());

		c.revalidate();
		c.repaint();
	}

	private CaptureData load(File file) {
		return load.loadFile(this, file);
	}

	private void initialize(File file) {
		load = new Load();
		if (file != null) {
			data = load(file);
		}
		view = new ShowObject();
		coor = new Coordinate(view);
		seg = new Segmentation(view);
		imgDepth = new ImageCapture(view, ImageCapture.DEPTH);
		imgColor = new ImageCapture(view, ImageCapture.COLOR);
		view.setCamera(ShowObject.DEPTH);
	}

	private JPanel getComponent() {
		JPanel c = new JPanel(new GridLayout(1, 0));
		c.setBorder(new TitledBorder("View"));
		c.add(view);
		c.setSize(640, 480);
		return c;
	}

	private JPanel getControl() {
		JPanel c = new JPanel(new BorderLayout());
		c.setBorder(new TitledBorder("Control"));
		c.setSize(640, 100);

		slider = new JSlider(0, data.getTimestamp().size());
		slider.setValue(0);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(25);
		slider.setPaintLabels(true);
		slider.addChangeListener(this);

		c.add(BorderLayout.CENTER, slider);
		return c;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == slider) {
			int index = slider.getValue();
			Long timestamp = data.getTimestampByIndex(index);

			view.setStatus("Time: " + timestamp);
			// view.setUserCoordinate(data.getCoordinateDepth().get(timestamp),
			// 0, 0);

			ByteBuffer buffSegmentation = data.getSegmentation().get(timestamp);

			byte[] buffBackground;
			if (view.getCamera() == ShowObject.COLOR) {
				buffBackground = data.getImageColor().get(timestamp);
			} else if (view.getCamera() == ShowObject.DEPTH) {
				buffBackground = data.getImageDepth().get(timestamp);
			} else {
				return;
			}

			if (buffSegmentation != null){
				buffSegmentation.rewind();
			}

			view.setUserMap(buffSegmentation);
			view.setBackground(ByteBuffer.wrap(buffBackground).order(ByteOrder.LITTLE_ENDIAN), 640, 480);
			view.repaint();

		}
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new SimpleEditor(new File("/home/matheus/Música/Olá"));
			}
		});
	}

}
