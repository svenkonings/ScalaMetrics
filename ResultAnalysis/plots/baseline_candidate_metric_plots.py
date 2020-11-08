import argparse

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

from analysis import categories
from plots import savefig


def main(args):
    for category in categories:
        plot_all(category, args)


def plot_all(category, args):
    zuilhof = get_results('multiparadigm-zuilhof', category)
    constructs = get_results('multiparadigm-constructs', category)

    if zuilhof is not None and constructs is not None:
        df = zuilhof.append(constructs, ignore_index=True)
        plot_barcharts('barchart', category, args, df)


def plot_barcharts(folder, category, args, df):
    width = 0.4
    baseline_data = df[df['name'].str.endswith('baseline')]
    candidate_data = df[~df['name'].str.endswith('baseline')]
    ind = np.arange(len(baseline_data))
    bars1 = plt.bar(ind + 0 * width, baseline_data['mcc mean'], width,
                    label='Without candidate')  # , yerr=baseline_data['mcc std'])
    bars2 = plt.bar(ind + 1 * width, candidate_data['mcc mean'], width,
                    label='With candidate')  # , yerr=candidate_data['mcc std'])
    plt.title(category)
    plt.ylabel('MCC')
    plt.ylim(0, 0.7)
    plt.xticks(ticks=ind + 0.5 * width, labels=candidate_data['name'], rotation=-30, ha='left')
    plt.grid(axis='y')
    plt.gcf().set_size_inches(16, 6)  # Double horizontal size
    plt.legend()

    counts = baseline_data['count'].values
    for i in range(len(baseline_data)):
        count = counts[i]
        bar1 = bars1.patches[i]
        bar2 = bars2.patches[i]
        x = bar2.get_x()
        y = max(bar1.get_y() + bar1.get_height(), bar2.get_y() + bar2.get_height())
        plt.annotate(str(count), (x, y), ha='center', va='bottom', size=9)
    savefig('multiparadigm', folder, category, '.pdf', args)


def get_results(folder, category):
    try:
        return pd.read_csv(
            f'../data/analysisResults/{folder}/regression/multivariate-baseline-hasdata/{category}/means.csv'
        )
    except FileNotFoundError:
        return None


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    parser.add_argument('--write', help='Write plots', dest='write', action="store_true")
    main(parser.parse_args())
