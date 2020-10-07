import argparse
import re
from collections import defaultdict
from glob import glob

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

from analysis import categories
from plots import get_split_results, savefig


def main(args):
    for category in categories:
        plot_all(category, args)
        plot_balanced(category, args)


def plot_all(category, args):
    neutral = get_split_results(category, 'Neutral')
    oop = get_split_results(category, 'OOP')
    fp = get_split_results(category, 'FP')
    mix = get_split_results(category, 'Mix')
    if neutral is not None and oop is not None and fp is not None and mix is not None:
        plot_barcharts('barchart', category, args, neutral, oop, fp, mix)


def plot_barcharts(folder, category, args, neutral, oop, fp, mix):
    ind = np.arange(len(neutral))
    width = 0.2
    plt.bar(ind + 0 * width, neutral['mcc mean'], width, label='Neutral')  # , yerr=neutral['mcc std'])
    plt.bar(ind + 1 * width, oop['mcc mean'], width, label='OOP')  # , yerr=oop['mcc std'])
    plt.bar(ind + 2 * width, fp['mcc mean'], width, label='FP')  # , yerr=fp['mcc std'])
    plt.bar(ind + 3 * width, mix['mcc mean'], width, label='Mixed')  # , yerr=mix['mcc std'])
    plt.title(category)
    plt.ylabel('MCC')
    plt.ylim(-0.05, 0.5)
    plt.xticks(ticks=ind + 1.5 * width, labels=neutral['name'], rotation=-30, ha='left')
    plt.grid(axis='y')
    plt.gcf().set_size_inches(16, 6)  # Double horizontal size
    plt.legend()
    savefig('baseline', folder, category, '.pdf', args)


def plot_balanced(category, args):
    projects = get_projects(category)
    if projects:
        for project, paradigms in list(projects.items()):
            if len(paradigms) != 4:
                del projects[project]

        paradigm_means = {}
        columns = ['name', 'precision', 'recall', 'mcc']
        for paradigm in ['Neutral', 'OOP', 'FP', 'Mix']:
            df = pd.DataFrame(columns=columns)
            for files in projects.values():
                df = df.append(pd.read_csv(files[paradigm])[columns], ignore_index=True)
            paradigm_means[paradigm] = get_means(df)

        plot_barcharts('barchart-balanced', category, args, *paradigm_means.values())


def get_projects(category):
    files = glob(f'../data/analysisResults/baseline/split-regression/univariate/{category}/[!m]*.csv')
    project_regex = re.compile(f'{category}/(.*)(Neutral|OOP|FP|Mix)')
    projects = defaultdict(dict)
    for file in files:
        file = file.replace('\\', '/')
        search = project_regex.search(file)
        project = search.group(1)
        paradigm = search.group(2)
        projects[project][paradigm] = file
    return projects


def get_means(df):
    counts = df.groupby('name').size().to_frame('count')
    means = df.groupby('name').agg(['mean', 'std'])
    means.columns = means.columns.map(' '.join)
    means = counts.join(means).reset_index()
    return means


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    parser.add_argument('--write', help='Write plots', dest='write', action="store_true")
    main(parser.parse_args())
