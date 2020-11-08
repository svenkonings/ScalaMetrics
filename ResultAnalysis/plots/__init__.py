import os

import matplotlib.pyplot as plt


def savefig(kind, directory, filename, extension, args):
    if args.write:
        directory = f'../data/analysisResults/{kind}/plots/{directory}/'
        os.makedirs(directory, exist_ok=True)
        plt.savefig(
            directory + filename + extension,
            bbox_inches='tight',
            metadata={'Creator': None, 'Producer': None, 'CreationDate': None}
        )
    if args.show:
        plt.show()
    plt.close()
