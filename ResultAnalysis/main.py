import os

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


def save_dataframe(df, directory, filename, save_index=True):
    os.makedirs(directory, exist_ok=True)
    df.to_csv(directory + filename + '.csv', index=save_index)
