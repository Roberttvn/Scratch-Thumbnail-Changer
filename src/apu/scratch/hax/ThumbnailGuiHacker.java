/**
 * 
 */
package apu.scratch.hax;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import apu.scratch.hax.GuiHackerProjectsView.Project;

/**
 * A GUI hacker for changing thumbnails
 * 
 * @author "MegaApuTurkUltra"
 * 
 */
public class ThumbnailGuiHacker extends JFrame {
	private static final long serialVersionUID = 478186628181192812L;
	private JPanel contentPane;
	private GuiHackerLogin login;
	private GuiHackerProjectsView projects;
	private GuiHackerFileChooser chooser;
	public static volatile ThumbnailGuiHacker INSTANCE;
	public static Project selectedProject;
	static JDialog loading;
	static JProgressBar loadingP;

	static {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.err.println("Uncaught exception on thread: " + t);
				e.printStackTrace();
				if (ThumbnailGuiHacker.INSTANCE != null) {
					ThumbnailGuiHacker.INSTANCE.showExceptionDialog(e);
				}
			}
		});
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loading = new JDialog(null,
						"Super swaggy hacks in progress...",
						ModalityType.APPLICATION_MODAL);
				loading.setLayout(new BorderLayout());
				loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				loadingP = new JProgressBar();
				loadingP.setIndeterminate(true);
				loading.add(loadingP, BorderLayout.CENTER);
				loading.setSize(300, 60);
				loading.setLocationRelativeTo(null);
				loading.setType(Window.Type.UTILITY);
				loading.setVisible(true);
			}
		});
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					INSTANCE = new ThumbnailGuiHacker();
					loading.setVisible(false);
					INSTANCE.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ThumbnailGuiHacker() {
		setTitle("MegaApuTurkUltra - Thumbnail GUI Hacker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		login = new GuiHackerLogin();
		contentPane.add(login, BorderLayout.CENTER);

		projects = new GuiHackerProjectsView();
		chooser = new GuiHackerFileChooser();

		setLocationRelativeTo(null);
	}

	public boolean initAndLogin(String username, char[] password) {
		setLoadingState(true);
		GuiHackerBackend.reset();
		try {
			GuiHackerBackend.init();
			GuiHackerBackend.login(username, password);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			setLoadingState(false);
			showExceptionDialog(e);
		} finally {
			setLoadingState(false);
		}
		return false;
	}

	public void setLoadingState(final boolean state) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loading.setLocationRelativeTo(ThumbnailGuiHacker.this);
				loading.setVisible(state);
			}
		});
	}

	public void showExceptionDialog(Throwable e) {
		String message = null;
		int x = JOptionPane.showOptionDialog(this, "An error has occurred: "
				+ (((message = e.getMessage()) == null) ? "Unknown" : message),
				"Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,
				null, new Object[] { "OK", "View Details", "Exit" }, 0);
		if (x == 1) {
			StringWriter stack = new StringWriter();
			e.printStackTrace(new PrintWriter(stack));
			JOptionPane.showMessageDialog(this, stack.toString(),
					"Error Details", JOptionPane.ERROR_MESSAGE);
			showExceptionDialog(e);
		}
		if (x == 2)
			System.exit(-1);
	}

	public void success() {
		setLoadingState(false);
		int x = JOptionPane
				.showOptionDialog(
						this,
						"Success! Thumbnail has been changed.\nRemember that opening "
								+ "the editor on your project\nwill change the thumbnail again.",
						"Swag", JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null,
						new Object[] { "Got it; Exit this app",
								"Change another one!" }, 0);
		if (x == 0)
			System.exit(0);
		else
			goToProjects();
	}

	public boolean warnAnnoyingGif() {
		return JOptionPane
				.showOptionDialog(
						this,
						"Warning: That GIF has a framerate that's too high, and may be annoying.",
						"Warning", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, new Object[] {
								"I'll choose another GIF",
								"I understand, proceed" }, 0) == 1;
	}

	public void goToProjects() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setTitle("Thumbnail Hacker - Select a project:");
				contentPane.remove(chooser);
				contentPane.remove(login);
				contentPane.add(projects, BorderLayout.CENTER);
				ThumbnailGuiHacker.this.revalidate();
				ThumbnailGuiHacker.this.repaint();
			}
		});
		setLoadingState(true);
		try {
			JSONArray projects = GuiHackerBackend.getProjects();
			for (int i = 0; i < projects.length(); i++) {
				JSONObject obj = projects.getJSONObject(i);
				int id = obj.getInt("pk");
				String title = obj.getJSONObject("fields").getString("title");
				this.projects.listModel.addElement(new Project(title, id));
			}
			this.projects.list.setEnabled(true);
			this.projects.list.setModel(this.projects.listModel);
			this.projects.btnChangeIt.setEnabled(true);
			this.projects.revalidate();
		} catch (Exception e) {
			e.printStackTrace();
			setLoadingState(false);
			showExceptionDialog(e);
		} finally {
			setLoadingState(false);
		}
	}

	public void goToFileChooser(Project selected) {
		setTitle("Thumbnail Hacker - Choose a new thumbnail:");
		selectedProject = selected;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				contentPane.remove(projects);
				contentPane.add(chooser, BorderLayout.CENTER);
				ThumbnailGuiHacker.this.revalidate();
			}
		});
	}

	public void executeHack(final File selected, final String mimeType) {
		setLoadingState(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					loadingP.setIndeterminate(false);
					GuiHackerBackend.hackThumbnail(selectedProject.id,
							selected, mimeType);
					loadingP.setIndeterminate(true);
					loading.setVisible(false);
					success();
				} catch (Exception e) {
					e.printStackTrace();
					setLoadingState(false);
					showExceptionDialog(e);
				} finally {
					setLoadingState(false);
				}
			}
		}).start();
	}
	
	public void setProgress(int progress){
		loadingP.setValue(progress);
	}
}
