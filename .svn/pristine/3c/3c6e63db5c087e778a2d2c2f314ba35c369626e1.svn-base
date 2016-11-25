BIBS=$(wildcard *.bib)
PLATFORM=$(shell /usr/bin/uname)

# this is super conservative, but it *ALWAYS WORKS*
document.pdf: *.tex $(BIBS)
	pdflatex document
	bibtex document
	pdflatex document
	pdflatex -synctex=-1 document

force: 
	pdflatex document
	bibtex document
	pdflatex document
	pdflatex -synctex=-1 document

view: document.pdf
ifeq "$(PLATFORM)" "Darwin"
		./skimview document.pdf
endif

clean:
	rm -f *bbl *aux *blg *log document.pdf
