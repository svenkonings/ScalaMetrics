import argparse
import os

import matplotlib.pyplot as plt
import numpy as np
from matplotlib.colors import LinearSegmentedColormap, LogNorm

from main import projects, get_metric_results


def to_color(faults):
    if faults == 0:
        return 'blue'
    else:
        return 'red'


def scatter(df, subfolder, name, x_axis, y_axis):
    df = df.round({x_axis: 1, y_axis: 1})
    df = df.groupby([x_axis, y_axis, ]).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    savefig('scatter/' + subfolder, name, '.pdf')


def scatter_faults(df, subfolder, name, x_axis, y_axis):
    df = df.round({x_axis: 1, y_axis: 1})
    df['color'] = df['faults'].apply(to_color)
    df = df.groupby([x_axis, y_axis, 'color']).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, c=df['color'], s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    savefig('scatter-faults/' + subfolder, name, '.pdf')


def scatter_color(df, subfolder, name, x_axis, y_axis):
    df = df.round({x_axis: 1, y_axis: 1})
    df = df.groupby([x_axis, y_axis, ]).size().reset_index(name='count')
    cmap = LinearSegmentedColormap.from_list('gyr', [(0, 'green'), (0.5, 'yellow'), (1, 'red')], N=256)
    df.plot.scatter(x_axis, y_axis, c=df['count'], cmap=cmap, alpha=0.5, norm=LogNorm(),
                    edgecolors='none', s=12.75, marker="s")
    plt.xlim(-0.25, 7.69)
    plt.ylim(-0.25, 7.13)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    savefig('scatter-color/' + subfolder, name, '.pdf')


def hist_faults(df, subfolder, name, axis):
    non_faulty = df[df['faults'] == 0]
    faulty = df[df['faults'] > 0]
    plt.hist(np.array([faulty[axis], non_faulty[axis]], dtype=object), bins=10, stacked=True,
             color=['lightcoral', 'darkseagreen'])
    plt.xlabel('Paradigm score')
    plt.ylabel('Occurrences')
    plt.title(name)
    savefig('hist-faults/' + subfolder, name, '.pdf')


def savefig(dirictory, filename, extension):
    dirictory = f'../data/analysisResults/plots/{dirictory}'
    os.makedirs(dirictory, exist_ok=True)
    plt.savefig(dirictory + filename + extension, bbox_inches='tight')
    if args.show:
        plt.show()
    plt.close()


def plot_functions(project, name):
    df = get_metric_results('paradigmScore', project, 'methodResultsBriand')
    name = name + ' functions'
    subfolder = 'functions/'
    # scatter(df, subfolder, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    # scatter_faults(df, subfolder, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    scatter_color(df, subfolder, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    hist_faults(df, subfolder, name, 'ParadigmScoreFraction')

    df = df[df['HasPointsFraction'] == 1]
    subfolder = 'functions_with_points/'
    scatter_color(df, subfolder, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    hist_faults(df, subfolder, name, 'ParadigmScoreFraction')



def plot_objects(project, name):
    df = get_metric_results('paradigmScore', project, 'objectMethodResultsBriand')
    name = name + ' objects'
    subfolder = 'objects/'
    # scatter(df, subfolder, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    # scatter_faults(df, subfolder, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    scatter_color(df, subfolder, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    hist_faults(df, subfolder, name, 'ParadigmScoreFractionAvr')

    df = df[df['HasPointsFractionMax'] == 1]
    subfolder = 'objects_with_points/'
    scatter_color(df, subfolder, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    hist_faults(df, subfolder, name, 'ParadigmScoreFractionAvr')


def main():
    for project, name in projects.items():
        plot_functions(project, name)
        plot_objects(project, name)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    args = parser.parse_args()
    main()
