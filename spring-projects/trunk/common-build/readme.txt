This is a common-build system. If only one project is using it, then it 
generally makes the most sense to put it in a directory called something like 
'common-build', living under the project root. Most dependencies are expected to 
come in via Ivy from iBiblio or another web-based shared repository, or possibly 
from a network-shared filesystem repository. However, in the single project case 
you probably also want to also have a static filesystem repository, called 
something like 'repository', right in the project dir parallel to the 
'common-build' dir, or right inside the 'common-build' dir itself.

As soon as this is going to be shared by more than one project (which is very 
handy, as projects can then have dependencies between them too), then it makes 
sense to move this out to be parallel in the directory tree with the projects 
using it, and move the shared filesystem repository parallel to that.

Projects using this need only a very minimal build.xml file. They may still override
and add to targets in the common-targets.xml, by using the dependency mechanism to
hook themselves in as needed. For example, to hook into the normal 'statics' target,
just do
  <target name="statics" depends="common-targets.statics">
this will insert the custom 'statics' target in the project's build.xml after the
the standard one, still running the standard one first. If there is a need to
insert before it, just depend on the 'pre' target instead:
  <target name="statics" depends="common-targets.statics.pre">

Important: if you are running Ant directly, you must drop the Ivy jar (i.e. ivy-xx.jar)
into ant's lib dir. All other binaries will be pulled down by the build as per the
dependency declaration in the project ivy.xml files. The ivy jar may be found in the
jar repository.

