/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kola;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author lkroll
 */
@Mojo(name = "kolac", requiresProject = true, threadSafe = false, requiresDependencyResolution = COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class KolaMojo extends AbstractMojo {

    /**
     * Skip plug-in execution.
     */
    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    /**
     * Target directory for generated Java source files.
     */
    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources")
    private File outputDirectory;

    /**
     * Directory location of the RAML file(s).
     */
    @Parameter(property = "sourceDirectory", defaultValue = "${basedir}/src/main/kola")
    private File sourceDirectory;

    /**
     * Whether to empty the output directory before generation occurs, to clear
     * out all source files
     * that have been generated previously.
     */
    @Parameter(property = "removeOldOutput", defaultValue = "false")
    private boolean removeOldOutput;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution...");
            return;
        }

        try {
            FileUtils.forceMkdir(outputDirectory);
        } catch (final IOException ioe) {
            throw new MojoExecutionException("Failed to create directory: " + outputDirectory, ioe);
        }

        if (removeOldOutput) {
            try {
                FileUtils.cleanDirectory(outputDirectory);
            } catch (final IOException ioe) {
                throw new MojoExecutionException("Failed to clean directory: " + outputDirectory, ioe);
            }
        }
        project.getCompileSourceRoots().clear();
        project.addCompileSourceRoot(outputDirectory.getPath());

        ClassWorld world = new ClassWorld();
        ClassRealm realm;
        try {
            realm = world.newRealm("kolac", null);
            for (String elt : project.getCompileSourceRoots()) {
                URL url = new File(elt).toURI().toURL();
                realm.addConstituent(url);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Source root: " + url);
                }
            }
            for (String elt : project.getCompileClasspathElements()) {
                URL url = new File(elt).toURI().toURL();
                realm.addConstituent(url);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Compile classpath: " + url);
                }
            }
        } catch (DuplicateRealmException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        Thread.currentThread().setContextClassLoader(realm.getClassLoader()); 
        
        Main.main(new String[]{"-s", outputDirectory.getPath(), sourceDirectory.getPath()});
    }

}
