import re
from optparse import OptionParser

foundTest = False
foundTestName = ""
class Test:
	def __init__(self, name, file):
		self.failed = False
		self.lines = []
		self.name = name
		for line in file:
			lineForHtml = line.replace(">","&gt;")
			lineForHtml = lineForHtml.replace("<","&lt;")		
			self.lines.append(lineForHtml)
			if re.match("^==== Test generated error:", line):
				self.failed = True
			if line == "\"uplevel $contents_of_test\"\n":
				break
			if re.match("(^---- .* FAILED|^Failed: 666)", line):
				self.failed = True
				break			
			if re.match("^Failed: [0-9]+  Total Tests: [0-9]+  \(\(Passed: [0-9]+, Newly Passed: [0-9]+\)  Known Failed: [0-9]+\)", line):
				#if re.match("^Failed: [1-9]", line):
				if re.match("^Failed: 666", line):
					self.failed = True
				break;
			if re.match("^\+\+\+\+ .+ PASSED\n$", line):
				break
			if re.match("^[a-zA-Z]+\.tcl\n$", line):
				break
			p = re.compile("^------------------.* testing ")	
			if p.match(line):
				foundTest = True
				foundTestName = p.split(line)[-1]
				break

if __name__=="__main__":
	parser = OptionParser("usage: %prog [options]")
	
	parser.add_option("-s", "--sourcefile", dest="source", help="The source file name",
                  metavar="FILE")
	parser.add_option("-t", "--targetfile", dest="target", help="The target html file name",
                  metavar="FILE")

	(options, args) = parser.parse_args()
	if options.source == None or options.target == None:
		parser.print_help()
		exit()

	file=open(options.source, 'r')

	FailedTests = []
	for line in file:
		p = re.compile("(^==== |^------------------.* testing )")
		if p.match(line):
			test = Test(p.split(line)[-1], file)
			if test.failed:
				FailedTests.append(test)
		if foundTest:
			foundTest = False
			test = Test(foundTestName, file)
			if test.failed:
				FailedTests.append(test)

	file.close()

	file=open(options.target, 'w')
	title = "Failed tests"
	file.write("<html><head><title>%s</title></head><body><ul><h1>%s</h1>" %(title, title))
	i = 0
	for test in FailedTests:
		file.write("<li><a href=\"#%d\">%s</a></li>" %(i, test.name))
		i = i + 1
	file.write("</ul>")
	i = 0
	for test in FailedTests:
		file.write("<h3><a name=\"%d\">%s</a></h3><a href=\"#top\">To top</a> | <a href=\"#%d\">Previous test</a> | <a href=\"#%d\">Next test</a><pre>" %(i,test.name, i - 1, i +1))
		for line in test.lines:
			file.write(line)
		file.write("</pre><a href=\"#top\">To top</a> | <a href=\"#%d\">Previous test</a> | <a href=\"#%d\">Next test</a>")
		i = i + 1
	file.write("</body></html>")
	file.close()

