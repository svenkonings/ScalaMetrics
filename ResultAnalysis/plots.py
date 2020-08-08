import argparse
import os

import matplotlib.colors
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def to_color(faults):
    if faults == 0:
        return 'blue'
    else:
        return 'red'


def scatter(df, name, x_axis, y_axis):
    df = df.round({x_axis: 1, y_axis: 1})
    df = df.groupby([x_axis, y_axis, ]).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    savefig('fig/scatter/', name, '.pdf')


def scatter_faults(df, name, x_axis, y_axis):
    df = df.round({x_axis: 1, y_axis: 1})
    df['faults'] = df['faults'].apply(to_color)
    df = df.groupby([x_axis, y_axis, 'faults']).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, c=df['faults'], s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    savefig('fig/scatter-faults/', name, '.pdf')


def scatter_color(df, name, x_axis, y_axis):
    df = df.round({x_axis: 1, y_axis: 1})
    df = df.groupby([x_axis, y_axis, ]).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, c=df['count'], cmap='RdYlGn_r', alpha=0.5, norm=matplotlib.colors.LogNorm(),
                    edgecolors='none')
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    savefig('fig/scatter-color/', name, '.pdf')


def hist_faults(df, name, axis):
    non_faulty = df[df['faults'] == 0]
    faulty = df[df['faults'] > 0]
    plt.hist(np.array([faulty[axis], non_faulty[axis]], dtype=object), bins=10, stacked=True,
             color=['lightcoral', 'darkseagreen'])
    plt.xlabel('Paradigm score')
    plt.ylabel('Occurrences')
    plt.title(name)
    savefig('fig/hist-faults/', name, '.pdf')


def savefig(dirictory, filename, extension):
    os.makedirs(dirictory, exist_ok=True)
    plt.savefig(dirictory + filename + extension, bbox_inches='tight')
    if args.show:
        plt.show()
    plt.close()


projects = {
    'akka': 'Akka',
    'coursier': 'Coursier',
    'gitbucket': 'Gitbucket',
    'http4s': 'Http4s',
    'lagom': 'Lagom',
    'quill': 'Quill',
    'scalafmt': 'scalafmt',
    'scala-js': 'Scala.js',
    'scio': 'Scio',
    'shapeless': 'Shapeless',
    'slick': 'Slick',
    'zio': 'ZIO',
}


def plot_functions(path, name):
    path = f'../target/{path}/functionResultsBriand.csv'
    name = name + ' functions'
    df = pd.read_csv(path)
    # scatter(df, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    # scatter_faults(df, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    scatter_color(df, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    hist_faults(df, name, 'ParadigmScoreFraction')


def plot_objects(path, name):
    path = f'../target/{path}/objectResultsBriand.csv'
    name = name + ' objects'
    df = pd.read_csv(path)
    # scatter(df, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    # scatter_faults(df, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    scatter_color(df, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    hist_faults(df, name, 'ParadigmScoreFractionAvr')


def main():
    for path, name in projects.items():
        plot_functions(path, name)
        plot_objects(path, name)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    args = parser.parse_args()
    main()
