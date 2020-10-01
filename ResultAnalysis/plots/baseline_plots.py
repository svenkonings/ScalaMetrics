import argparse

import matplotlib.pyplot as plt
import numpy as np

from analysis import categories
from plots import get_split_results, savefig


def main(args):
    for category in categories:
        neutral = get_split_results(category, 'Neutral')
        oop = get_split_results(category, 'OOP')
        fp = get_split_results(category, 'FP')
        mix = get_split_results(category, 'Mix')
        if neutral is None or oop is None or fp is None or mix is None:
            continue

        ind = np.arange(len(neutral))
        width = 0.2
        plt.bar(ind + 0 * width, neutral['mcc mean'], width, label='Neutral')  # , yerr=neutral['mcc std'])
        plt.bar(ind + 1 * width, oop['mcc mean'], width, label='OOP')  # , yerr=oop['mcc std'])
        plt.bar(ind + 2 * width, fp['mcc mean'], width, label='FP')  # , yerr=fp['mcc std'])
        plt.bar(ind + 3 * width, mix['mcc mean'], width, label='Mixed')  # , yerr=mix['mcc std'])
        plt.title(category)
        plt.ylabel('MCC')
        plt.ylim(-0.05, 0.45)
        plt.xticks(ticks=ind + 1.5 * width, labels=neutral['name'], rotation=-30, ha='left')
        plt.grid(axis='y')
        plt.gcf().set_size_inches(16, 6)  # Double horizontal size
        plt.legend()
        savefig('baseline', 'barchart', category, '.pdf', args)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--show', help='Show plots', dest='show', action="store_true")
    parser.add_argument('--write', help='Write plots', dest='write', action="store_true")
    main(parser.parse_args())
