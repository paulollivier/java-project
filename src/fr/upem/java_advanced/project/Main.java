package fr.upem.java_advanced.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import fr.upem.java_advanced.project.args.Args;
import fr.upem.java_advanced.project.args.ValidationError;
import fr.upem.java_advanced.project.zip.ArchiveChecker;
import fr.upem.java_advanced.project.zip.Zip;
import fr.upem.java_advanced.project.io.Messages;

public class Main {

	private static final Logger logger = Logger
			.getLogger("fr.upem.java_advanced.project");
	private static final JSAP jsap = new JSAP();
	public static JSAPResult cliArgs = null;

	static {
		/* cli args definition */

		Switch checkArchive = new Switch("checkArchive").setShortFlag('1')
				.setLongFlag("check-archive");
		checkArchive
				.setHelp("Vérifie que les fichiers donnés en paramètre sont bien des fichiers ZIP.");
		Switch archiveOfArchives = new Switch("archiveOfArchives")
				.setShortFlag('2').setLongFlag("archive-of-archives");
		archiveOfArchives
				.setHelp("Vérifie que les fichiers donnés en paramètre sont bien des archives ZIP d'archives ZIP, et fait comme si le flag -1 était passé pour ces archives.");

		Switch verbose = new Switch("verbosity").setShortFlag('v').setLongFlag(
				"verbose");
		Switch debug = new Switch("debug").setLongFlag("debug");

		FlaggedOption onetop = new FlaggedOption("onetop").setShortFlag('o')
				.setLongFlag("onetop").setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(false).setRequired(false);

		onetop.setHelp("Un seul sous répertoire de nom <onetop> dans le répertoire racine de l'archive sans compter les répertoires et fichiers ignorés comme : './ '");
		FlaggedOption forceOnetop = new FlaggedOption("forceonetop")
				.setShortFlag('O').setLongFlag("forceonetop")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(false).setRequired(false);
		forceOnetop
				.setHelp("Comme -o. Si le motif est trouvé, le programme quittera avec erreur.");

		FlaggedOption endsWith = new FlaggedOption("endsWith")
				.setShortFlag('e').setLongFlag("endsWith")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		endsWith.setHelp("les fichiers dont le nom se termine par la <endswith>  sont interdits par exemple: -e ~ pour les noms de fichiers terminant par ~ ou -e __MACOSX ces fichiers/répertoires seront ignorés à la décompression");
		FlaggedOption forceEndsWith = new FlaggedOption("forceendsWith")
				.setShortFlag('E').setLongFlag("forceendsWith")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		forceEndsWith
				.setHelp("Comme -e. Si le motif est trouvé, le programme quittera avec erreur.");

		FlaggedOption beginsWith = new FlaggedOption("beginsWith")
				.setShortFlag('b').setLongFlag("beginsWith")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		beginsWith
				.setHelp("les fichiers/répertoires commençant par <startswith> sont interdits. Par exemple --beginsWith f_ pour les noms de  fichiers commençant par f_");
		FlaggedOption forceBeginsWith = new FlaggedOption("forcebeginsWith")
				.setShortFlag('B').setLongFlag("forcebeginsWith")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		forceBeginsWith
				.setHelp("Comme -b. Si le motif est trouvé, le programme quittera avec erreur.");

		FlaggedOption existe = new FlaggedOption("existe").setShortFlag('x')
				.setLongFlag("existe").setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		existe.setHelp("Vérifie la présence du fichier ou répertoire (regex). Le répertoire top level est ignoré dans la comparaison. Par exemple:-x index.html");
		FlaggedOption forceExiste = new FlaggedOption("forceexiste")
				.setShortFlag('X').setLongFlag("forceexiste")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		forceExiste
				.setHelp("Comme -x. Si le motif est trouvé, le programme quittera avec erreur.");

		FlaggedOption interdit = new FlaggedOption("interdit")
				.setShortFlag('i').setLongFlag("interdit")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		interdit.setHelp("Vérifie l'absence du fichier ou répertoire de regex <interdit> le repertoire top level est ignoré dans la comparaison. Par exemple : --interdit __MACOSX");
		FlaggedOption forceInterdit = new FlaggedOption("forceinterdit")
				.setShortFlag('I').setLongFlag("forceinterdit")
				.setStringParser(JSAP.STRING_PARSER)
				.setAllowMultipleDeclarations(true).setRequired(false);
		forceInterdit
				.setHelp("Comme -i. Si le motif est trouvé, le programme quittera avec erreur.");

		UnflaggedOption paths = new UnflaggedOption("paths")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setGreedy(true);
		try {
			/* generic flags */
			jsap.registerParameter(verbose);
			jsap.registerParameter(debug);

			/* running modes */
			jsap.registerParameter(checkArchive);
			jsap.registerParameter(archiveOfArchives);

			jsap.registerParameter(onetop);
			jsap.registerParameter(forceOnetop);

			jsap.registerParameter(endsWith);
			jsap.registerParameter(forceEndsWith);

			jsap.registerParameter(beginsWith);
			jsap.registerParameter(forceBeginsWith);

			jsap.registerParameter(existe);
			jsap.registerParameter(forceExiste);

			jsap.registerParameter(interdit);
			jsap.registerParameter(forceInterdit);

			/* the other parameters */
			jsap.registerParameter(paths);

		} catch (JSAPException e) {
			logger.severe("Could not register a cli parameter: "
					+ e.getMessage());
			System.exit(1);
		}
	}
	
	public static Args createCliArgsChainCheck(JSAPResult cli) {
		Args begin = null;
		List<ValidationError> fatalErrors = new ArrayList<>();
		String interdit[] = cli.getStringArray("interdit");
		if(interdit.length>0) {
			begin = new Args(
					(path) -> {
						List<ValidationError> errs = new ArrayList<>();
						for(String i:interdit) {
							if(path.toString().matches(i)) {
								errs.add(new ValidationError(Messages.getOutputString("i", path.toString())));
							}
						}
						return errs;
					}, null);
		}
		
		String forceinterdit[] = cli.getStringArray("forceinterdit");
		if(interdit.length > 0) {
			Args forceInterdit = new Args((Path path) -> {
				for(String fi: forceinterdit) {
					if(path.toString().matches(fi)) {
						fatalErrors.add(new ValidationError(Messages.getOutputString("forceinterdit", path.toString())));
					}
				}
				return fatalErrors;
			}, null);
			if(begin != null)
				begin.setNext(forceInterdit);
			else
				begin = forceInterdit;
		}
		return begin;
	}

	/**
	 * builds the help string
	 * 
	 * @return the help, as a String
	 */
	public static String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("Usage: DMChecker ");
		sb.append(jsap.getUsage());
		sb.append("\n\n");
		sb.append(jsap.getHelp());
		return sb.toString();
	}

	public static void main(String[] args) {
		cliArgs = jsap.parse(args);
		if (!cliArgs.success()) {
			for (@SuppressWarnings("rawtypes")
			java.util.Iterator errs = cliArgs.getErrorMessageIterator(); errs
					.hasNext();) {
				System.err.println("Error: " + errs.next());
			}
			System.err.println(getHelp());
			System.exit(1);
		}

		if (!cliArgs.getBoolean("checkArchive")
				&& !cliArgs.getBoolean("archiveOfArchives")) {
			logger.severe("Aucun mode opératoire (-1, -2, -3, -4) n'a été selectionné !");
			System.exit(1);
		}

		if (cliArgs.getBoolean("checkArchive")
				&& cliArgs.getBoolean("archiveOfArchives")) {
			logger.severe("Modes opératoires incompatibles détectés !");
			System.exit(1);
		}

		Level lvl = Level.WARNING;
		if (cliArgs.getBoolean("verbosity"))
			lvl = Level.INFO;
		if (cliArgs.getBoolean("debug"))
			lvl = Level.ALL;

		logger.setLevel(lvl);
		for (Handler h : logger.getHandlers()) {
			h.setLevel(lvl);
		}

		for (String s : cliArgs.getStringArray("archives")) {
			Path p = Paths.get(s);
			try {
				ArchiveChecker ac = new ArchiveChecker(p);
				ac.checkArchive();
			} catch (IOException e) {
				// Nothing for now, but TODO: add proper handling.
				e.printStackTrace();
			}
		}

		if (cliArgs.getBoolean("archiveOfArchives")) {
			String[] paths = cliArgs.getStringArray("paths");
			Path archive = Paths.get(paths[0]);
			String archiveName = archive.toFile().getAbsolutePath();
			Path dst = null;
			if (paths.length >= 2) {
				dst = Paths.get(Paths.get(paths[1])
						+ "/"
						+ archiveName.substring(
								archiveName.lastIndexOf("/") + 1, archive
										.toFile().getAbsolutePath()
										.lastIndexOf(".")));
			} else {
				dst = Paths.get(archiveName.substring(
						archiveName.lastIndexOf("/") + 1, archive.toFile()
								.getAbsolutePath().lastIndexOf(".")));
			}

			File dstFile = dst.toFile();
			dstFile.mkdir();

			Zip.extract(archive, dst);

			File[] archives = dstFile.listFiles();

			for (File f : archives) {
				Zip.extract(Paths.get(f.getPath()), dst);
			}
		}

		// // ///////////////////////////////// TESTS //
		// /////////////////////////////////////
		// Path archive = Paths.get(cliArgs.getString("archives"));
		// // Path folder = Paths.get(cliArgs.getStringArray("archives")[1]);
		// for (String s : cliArgs.getStringArray("archives")) {
		// Path p = Paths.get(s);
		// try {
		// ArchiveChecker.checkArchive(p);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// // Zip.extract(archive, folder);
	}
}
