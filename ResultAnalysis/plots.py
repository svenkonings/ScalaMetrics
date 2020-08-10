import argparse
import os

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib.colors import LinearSegmentedColormap, LogNorm

from main import projects


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
    savefig('fig/scatter/' + subfolder, name, '.pdf')


def scatter_faults(df, subfolder, name, x_axis, y_axis):
    df = df.round({x_axis: 1, y_axis: 1})
    df['color'] = df['faults'].apply(to_color)
    df = df.groupby([x_axis, y_axis, 'color']).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, c=df['color'], s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    savefig('fig/scatter-faults/' + subfolder, name, '.pdf')


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
    savefig('fig/scatter-color/' + subfolder, name, '.pdf')


def hist_faults(df, subfolder, name, axis):
    non_faulty = df[df['faults'] == 0]
    faulty = df[df['faults'] > 0]
    plt.hist(np.array([faulty[axis], non_faulty[axis]], dtype=object), bins=10, stacked=True,
             color=['lightcoral', 'darkseagreen'])
    plt.xlabel('Paradigm score')
    plt.ylabel('Occurrences')
    plt.title(name)
    savefig('fig/hist-faults/' + subfolder, name, '.pdf')


def savefig(dirictory, filename, extension):
    os.makedirs(dirictory, exist_ok=True)
    plt.savefig(dirictory + filename + extension, bbox_inches='tight')
    if args.show:
        plt.show()
    plt.close()


def plot_functions(path, name):
    path = f'../target/{path}/functionResultsBriand.csv'
    name = name + ' functions'
    df = pd.read_csv(path)
    subfolder = 'functions/'
    # scatter(df, subfolder, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    # scatter_faults(df, subfolder, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    scatter_color(df, subfolder, name, 'FunctionalScoreFraction', 'ImperativeScoreFraction')
    hist_faults(df, subfolder, name, 'ParadigmScoreFraction')


def plot_objects(path, name):
    path = f'../target/{path}/objectResultsBriand.csv'
    name = name + ' objects'
    df = pd.read_csv(path)
    subfolder = 'objects/'
    # scatter(df, subfolder, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    # scatter_faults(df, subfolder, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    scatter_color(df, subfolder, name, 'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    hist_faults(df, subfolder, name, 'ParadigmScoreFractionAvr')


def main():
    for path, name in projects.items():
        plot_functions(path, name)
        plot_objects(path, name)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    args = parser.parse_args()
    main()
