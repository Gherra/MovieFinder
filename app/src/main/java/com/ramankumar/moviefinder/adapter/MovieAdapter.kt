package com.ramankumar.moviefinder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ramankumar.moviefinder.R
import com.ramankumar.moviefinder.model.Movie

class MovieAdapter(
    private var movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit,
    private val onFavoriteClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]

        holder.titleTextView.text = movie.title
        holder.yearTextView.text = movie.releaseDate.take(4)
        holder.ratingTextView.text = "⭐ ${String.format("%.1f", movie.voteAverage)}"

        // Load poster with Glide
        Glide.with(holder.itemView.context)
            .load(movie.getPosterUrl())
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.posterImageView)

        // Set favorite icon
        if (movie.isFavorite) {
            holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_off)
        }

        // Click listeners
        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(movie)
        }
    }

    override fun getItemCount() = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}