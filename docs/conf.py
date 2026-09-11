#  Copyright (C) 2020  Consiglio Nazionale delle Ricerche
#
#      This program is free software: you can redistribute it and/or modify
#      it under the terms of the GNU Affero General Public License as
#      published by the Free Software Foundation, either version 3 of the
#      License, or (at your option) any later version.
#
#      This program is distributed in the hope that it will be useful,
#      but WITHOUT ANY WARRANTY; without even the implied warranty of
#      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#      GNU Affero General Public License for more details.
#
#      You should have received a copy of the GNU Affero General Public License
#      along with this program.  If not, see <https://www.gnu.org/licenses/>.

import docs_theme

project = 'SIGLA'
release = '8.0.26'
author = u'Istituto Superiore di Sanità'

show_authors = True
# Grouping the document tree into LaTeX files. List of tuples
# (source start file, target name, title, author, documentclass [howto/manual]).
latex_documents = [
    ('index', 'Manuale-SIGLA.tex', project, author, 'manual'),
]
latex_elements = {
    'extraclassoptions': 'openany,oneside',
    'fontpkg': '',
}
epub_basename = u'Manuale-SIGLA'

html_theme = "docs_theme"
html_theme_path = [docs_theme.get_html_theme_path()]
# These folders are copied to the documentation's HTML output
html_static_path = ['_static']

copyright = "2020 Istituto Superiore di Sanità"
html_title = "SIGLA"
html_show_sourcelink = False
html_favicon = "favicon.ico"
html_logo = "logo.png"
latex_logo = 'logo.png'
html_baseurl = 'docs'
smartquotes = False
language = "it"
numfig = True
# The master toctree document.
master_doc = 'index'
source_suffix = {
    '.rst': 'restructuredtext',
    '.md': 'markdown',
}
# These folders are copied to the documentation's HTML output
templates_path = ['_templates']

extensions = [
    'sphinx.ext.autodoc',
    'sphinx.ext.doctest',
    'sphinx.ext.intersphinx',
    'sphinx.ext.todo',
    'sphinx.ext.coverage',
    'sphinx.ext.ifconfig',
    'myst_parser',
    'docs_theme'
]
